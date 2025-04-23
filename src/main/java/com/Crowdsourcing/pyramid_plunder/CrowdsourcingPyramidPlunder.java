package com.Crowdsourcing.pyramid_plunder;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import static net.runelite.api.MenuAction.GAME_OBJECT_FIRST_OPTION;
import static net.runelite.api.MenuAction.GAME_OBJECT_SECOND_OPTION;
import static net.runelite.api.MenuAction.NPC_THIRD_OPTION;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingPyramidPlunder
{

	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;


	private final WorldArea inside = new WorldArea(1916, 4418, 70, 62, 0);
	private final WorldArea insideFloor1 = new WorldArea(1916, 4418, 70, 62, 1);
	private final WorldArea insideFloor2 = new WorldArea(1916, 4418, 70, 62, 2);
	private final WorldArea outside = new WorldArea(3281, 2787, 16, 17, 0);

	ImmutableSet<Integer> varbsToTrack = ImmutableSet.<Integer>builder()
		// Urns
		.add(VarbitID.NTK_URN1_STATE)
		.add(VarbitID.NTK_URN2_STATE)
		.add(VarbitID.NTK_URN3_STATE)
		.add(VarbitID.NTK_URN4_STATE)
		.add(VarbitID.NTK_URN5_STATE)
		.add(VarbitID.NTK_URN6_STATE)
		.add(VarbitID.NTK_URN7_STATE)
		.add(VarbitID.NTK_URN8_STATE)
		.add(VarbitID.NTK_URN9_STATE)
		.add(VarbitID.NTK_URN10_STATE)
		.add(VarbitID.NTK_URN11_STATE)
		.add(VarbitID.NTK_URN12_STATE)
		.add(VarbitID.NTK_URN13_STATE)
		.add(VarbitID.NTK_URN14_STATE)
		// Sarcophagus
		.add(VarbitID.NTK_SARCOPHAGUS_STATE)
		// Grand gold chest
		.add(VarbitID.NTK_GOLDEN_CHEST_STATE).build();

	HashMap<Integer, Integer> varbVals = new HashMap<>();

	public void startUp()
	{
		if (client == null)
			return;

		// There's probably some way to init varbVals with these values so we don't need this function
		for (Integer varbIndex : varbsToTrack)
		{
			if (varbIndex == null)
				continue;
			varbVals.put(varbIndex, 0);
		}
	}

	private boolean isInPyramidPlunder(WorldPoint w)
	{
		if (w.isInArea(inside) || w.isInArea(insideFloor1) || w.isInArea(insideFloor2) || w.isInArea(outside))
			return true;
		return false;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (client == null)
			return;
		// Check each varb
		for (Integer varbIndex : varbsToTrack)
		{
			// If the varb has changed, update the value in the map and create a new data object to log
			int oldVarbVal = varbVals.get(varbIndex);
			int newVarbVal = client.getVarbitValue(varbIndex);
			if (newVarbVal != oldVarbVal)
			{
				// Create varb data
				varbVals.put(varbIndex, client.getVarbitValue(varbIndex));
				WorldPoint w = client.getLocalPlayer().getWorldLocation();
				int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
				int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
				// Only send data if we are in or right outside PP. Still track changes in map though
				if (isInPyramidPlunder(w))
				{
					PyramidPlunderVarbData data = new PyramidPlunderVarbData(varbIndex, oldVarbVal, newVarbVal, w, unboostedLevel, boostedLevel, client.getTickCount());
					//log.error(data.toString());
					manager.storeEvent(data);
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		String message = chatMessage.getMessage();
		if (client == null || client.getLocalPlayer() == null)
		{
			return;
		}
		int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
		int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
		WorldPoint w = client.getLocalPlayer().getWorldLocation();
		// Only store message info if we are in or nearby PP
		if (isInPyramidPlunder(w))
		{
			PyramidPlunderMessageData data = new PyramidPlunderMessageData(message, w, unboostedLevel, boostedLevel, client.getTickCount());
			// log.error(data.toString());
			manager.storeEvent(data);
		}
	}

	ImmutableSet<Integer> objectIdsToCheck = ImmutableSet.<Integer>builder()
		// outside doors
		.add(ObjectID.NTK_PYRAMID_DOOR_NORTH_ANIM).add(ObjectID.NTK_PYRAMID_DOOR_NORTH_NOANIM)
		.add(ObjectID.NTK_PYRAMID_DOOR_NORTH_OPEN_NOANIM).add(ObjectID.NTK_PYRAMID_DOOR_EAST_ANIM)
		.add(ObjectID.NTK_PYRAMID_DOOR_EAST_NOANIM).add(ObjectID.NTK_PYRAMID_DOOR_EAST_OPEN_NOANIM)
		.add(ObjectID.NTK_PYRAMID_DOOR_SOUTH_ANIM).add(ObjectID.NTK_PYRAMID_DOOR_SOUTH_NOANIM)
		.add(ObjectID.NTK_PYRAMID_DOOR_SOUTH_OPEN_NOANIM).add(ObjectID.NTK_PYRAMID_DOOR_WEST_ANIM)
		.add(ObjectID.NTK_PYRAMID_DOOR_WEST_NOANIM).add(ObjectID.NTK_PYRAMID_DOOR_WEST_OPEN_NOANIM)
		// spear trap
		.add(ObjectID.NTK_SPEARTRAP_INMOTION)
		// Grand gold chest
		.add(ObjectID.NTK_GOLDEN_CHEST_CLOSED).add(ObjectID.NTK_GOLDEN_CHEST_OPEN)
		// Sarcophagus
		.add(ObjectID.NTK_SARCOPHAGUS).add(ObjectID.NTK_SARCOPHAGUS_OPEN).add(ObjectID.NTK_SARCOPHAGUS_ANIM)
		// urns, urns, and more urns
		.add(ObjectID.NTK_URN1_CLOSED).add(ObjectID.NTK_URN2_CLOSED).add(ObjectID.NTK_URN3_CLOSED)
		.add(ObjectID.NTK_URN_ROUGH_CLOSED).add(ObjectID.NTK_URN1_OPEN).add(ObjectID.NTK_URN2_OPEN)
		.add(ObjectID.NTK_URN3_OPEN).add(ObjectID.NTK_URN_ROUGH_OPEN).add(ObjectID.NTK_URN1_SNAKE)
		.add(ObjectID.NTK_URN2_SNAKE).add(ObjectID.NTK_URN3_SNAKE).add(ObjectID.NTK_URN_ROUGH_SNAKE)
		.add(ObjectID.NTK_URN1_SNAKE_CHARMED).add(ObjectID.NTK_URN2_SNAKE_CHARMED).add(ObjectID.NTK_URN3_SNAKE_CHARMED)
		.add(ObjectID.NTK_URN_ROUGH_SNAKE_CHARMED)
		// inside doors
		.add(ObjectID.NTK_TOMB_DOOR_NOANIM).add(ObjectID.NTK_TOMB_DOOR_ANIM)
		.add(ObjectID.NTK_TOMB_DOOR_EXIT).add(ObjectID.NTK_ANTECHAMBER_EXIT).build();
	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event) {
		int tick = client.getTickCount();
		if (event.getMenuAction() == GAME_OBJECT_FIRST_OPTION || event.getMenuAction() == GAME_OBJECT_SECOND_OPTION)
		{
			ObjectComposition objectComposition = client.getObjectDefinition(event.getId());
			// If imposter ids is null, use the composition's id (loc is not a multiloc in this case)
			int id;
			if (objectComposition.getImpostorIds() == null)
				id = objectComposition.getId();
			else
				id = objectComposition.getImpostor().getId();
			if (objectIdsToCheck.contains(id))
			{
				int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
				int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
				WorldPoint w = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
				PyramidPlunderSceneryData data = new PyramidPlunderSceneryData(id, objectComposition.getId(), event.getMenuAction().getId(), w, unboostedLevel, boostedLevel, tick);
				// log.error(data.toString());
				manager.storeEvent(data);
			}
		}
		// Guardian mummy event
		else if (event.getMenuAction() == NPC_THIRD_OPTION && event.getMenuTarget().equals("<col=ffff00>Guardian mummy"))
		{
			int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
			int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
			WorldPoint w = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
			PyramidPlunderSceneryData data = new PyramidPlunderSceneryData(NpcID.NTK_MUMMY_GUARDIAN, NpcID.NTK_MUMMY_GUARDIAN, event.getMenuAction().getId(), w, unboostedLevel, boostedLevel, tick);
			// log.error(data.toString());
			manager.storeEvent(data);
		}
	}

}
