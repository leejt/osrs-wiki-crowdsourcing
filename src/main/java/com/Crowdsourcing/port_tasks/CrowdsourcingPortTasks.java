package com.Crowdsourcing.port_tasks;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingPortTasks
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	private Integer currentNoticeboard = null;
	private final List<Integer> offeredTasks = new ArrayList<>();
	private final List<Integer> lastOfferedTasks = new ArrayList<>();
	private final int[] currentTasks = new int[5];
	private final Deque<Integer> lastTasks = new ArrayDeque<>(20);

	private static final Set<Integer> NOTICEBOARDS = ImmutableSet.of(
		ObjectID.PORT_TASK_BOARD_PORT_SARIM, ObjectID.PORT_TASK_BOARD_PANDEMONIUM,
		ObjectID.PORT_TASK_BOARD_CATHERBY, ObjectID.PORT_TASK_BOARD_PORT_KHAZARD,
		ObjectID.PORT_TASK_BOARD_RUINS_OF_UNKAH, ObjectID.PORT_TASK_BOARD_RELLEKKA,
		ObjectID.PORT_TASK_BOARD_PORT_PISCARILIUS, ObjectID.PORT_TASK_BOARD_CIVITAS_ILLA_FORTIS,
		ObjectID.PORT_TASK_BOARD_LANDS_END, ObjectID.PORT_TASK_BOARD_MUSA_POINT,
		ObjectID.PORT_TASK_BOARD_ARDOUGNE, ObjectID.PORT_TASK_BOARD_BRIMHAVEN,
		ObjectID.PORT_TASK_BOARD_CORSAIR_COVE, ObjectID.PORT_TASK_BOARD_THE_SUMMER_SHORE,
		ObjectID.PORT_TASK_BOARD_ALDARIN, ObjectID.PORT_TASK_BOARD_VOID_KNIGHTS_OUTPOST,
		ObjectID.PORT_TASK_BOARD_PORT_ROBERTS, ObjectID.PORT_TASK_BOARD_RED_ROCK,
		ObjectID.PORT_TASK_BOARD_ETCETERIA, ObjectID.PORT_TASK_BOARD_PORT_TYRAS,
		ObjectID.PORT_TASK_BOARD_DEEPFIN_POINT, ObjectID.PORT_TASK_BOARD_PRIFDDINAS,
		ObjectID.PORT_TASK_BOARD_LUNAR_ISLE
	);

	private static final Map<Integer, Integer> TASK_SLOT_INDEX = Map.of(
		VarbitID.PORT_TASK_SLOT_0_ID, 0,
		VarbitID.PORT_TASK_SLOT_1_ID, 1,
		VarbitID.PORT_TASK_SLOT_2_ID, 2,
		VarbitID.PORT_TASK_SLOT_3_ID, 3,
		VarbitID.PORT_TASK_SLOT_4_ID, 4
	);

	@SuppressWarnings("unused")
	@Subscribe
	private void onMenuOptionClicked(final MenuOptionClicked event)
	{
		final int identifier = event.getId();
		if (NOTICEBOARDS.contains(identifier))
		{
			currentNoticeboard = identifier;
		}
	}

	@SuppressWarnings("unused")
	@Subscribe
	private void onVarbitChanged(final VarbitChanged event)
	{
		final int varbitId = event.getVarbitId();
		final int value = event.getValue();
		final Integer idx = TASK_SLOT_INDEX.get(varbitId);
		if (idx == null)
		{
			return; // Not a port task assignment
		}

		currentTasks[idx] = value;
		// A value of 0 means our task was completed and slot is empty
		// Only add actual assignments to the queue
		if (value > 0)
		{
			addTask(value);
		}
	}

	@SuppressWarnings("unused")
	@Subscribe
	private void onWidgetLoaded(final WidgetLoaded event)
	{
		if (event.getGroupId() != InterfaceID.PORT_TASK_BOARD)
		{
			return;
		}
		lastOfferedTasks.clear();
		lastOfferedTasks.addAll(offeredTasks);
		offeredTasks.clear();
		// Interface isn't ready on widget load and needs to be done later
		clientThread.invokeLater(this::scanPortTaskBoard);
	}

	private void scanPortTaskBoard()
	{
		final Widget widget = client.getWidget(InterfaceID.PortTaskBoard.CONTAINER);
		if (widget == null)
		{
			return;
		}
		List<Widget> children = new ArrayList<>();
		if (widget.getDynamicChildren() != null)
		{
			children.addAll(Arrays.asList(widget.getDynamicChildren()));
		}

		for (Widget child : children)
		{
			// The notice board is made up of 48 dynamic children
			if (child == null)
			{
				continue;
			}
			// The clickable tasks are the only widgets with opListeners
			Object[] ops = child.getOnOpListener();
			if (ops == null || ops.length < 4)
			{
				continue;
			}
			// The value of the last opListener is the dbrow of the offered task
			offeredTasks.add((Integer) ops[3]);
		}
		if (!offeredTasks.equals(lastOfferedTasks))
		{
			submitTasks();
		}
	}

	private void addTask(int value)
	{
		if (lastTasks.size() == 20)
		{
			lastTasks.removeFirst();
		}
		lastTasks.addLast(value);
	}

	private void submitTasks()
	{
		if (currentNoticeboard == null)
		{
			return;
		}

		PortTaskData data = new PortTaskData(
			client.getRealSkillLevel(Skill.SAILING),
			currentNoticeboard,
			Arrays.copyOf(currentTasks, currentTasks.length),
			List.copyOf(offeredTasks),
			new ArrayDeque<>(lastTasks)
		);
		manager.storeEvent(data);
	}
}
