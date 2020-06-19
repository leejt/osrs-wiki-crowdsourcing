package com.Crowdsourcing.zmi;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import javax.inject.Inject;
import java.util.Arrays;

@Slf4j
public class CrowdsourcingZMI
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private static final String CHAT_MESSAGE_ZMI = "You bind the temple's power into runes.";
	private int gameTickZMI = -1;
	private int illegalActionTick = -1;
	private int previousRunecraftXp = 0;
	private int runecraftXpGained = 0;
	private Multiset<Integer> previousInventorySnapshot;

	private Multiset<Integer> getInventorySnapshot()
	{
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		Multiset<Integer> inventorySnapshot = HashMultiset.create();

		if (inventory != null)
		{
			Arrays.stream(inventory.getItems())
				.forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
		}

		return inventorySnapshot;
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		MenuAction action = menuOptionClicked.getMenuAction();

		switch (action)
		{
			case ITEM_FIRST_OPTION:
			case ITEM_SECOND_OPTION:
			case ITEM_THIRD_OPTION:
			case ITEM_FOURTH_OPTION:
			case ITEM_FIFTH_OPTION:
			case GROUND_ITEM_FIRST_OPTION:
			case GROUND_ITEM_SECOND_OPTION:
			case GROUND_ITEM_THIRD_OPTION:
			case GROUND_ITEM_FOURTH_OPTION:
			case GROUND_ITEM_FIFTH_OPTION:
			case ITEM_DROP:
				illegalActionTick = client.getTickCount();
				break;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getMessage().equals(CHAT_MESSAGE_ZMI))
		{
			gameTickZMI = client.getTickCount();
			previousRunecraftXp = client.getSkillExperience(Skill.RUNECRAFT);
			previousInventorySnapshot = getInventorySnapshot();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (gameTickZMI == client.getTickCount())
		{
			int currentRunecraftXp = statChanged.getXp();
			runecraftXpGained = currentRunecraftXp - previousRunecraftXp;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int itemContainerChangedTick = client.getTickCount();

		if (event.getItemContainer() != client.getItemContainer(InventoryID.INVENTORY) || gameTickZMI != itemContainerChangedTick)
		{
			return;
		}

		int tickDelta = itemContainerChangedTick - illegalActionTick;
		boolean ardougneMedium = client.getVar(Varbits.DIARY_ARDOUGNE_MEDIUM) == 1;
		int runecraftBoostedLevel = client.getBoostedSkillLevel(Skill.RUNECRAFT);
		Multiset<Integer> currentInventorySnapshot = getInventorySnapshot();
		final Multiset<Integer> itemsReceived = Multisets.difference(currentInventorySnapshot, previousInventorySnapshot);
		final Multiset<Integer> itemsRemoved = Multisets.difference(previousInventorySnapshot,currentInventorySnapshot);

		ZMIData data = new ZMIData(tickDelta, ardougneMedium, runecraftBoostedLevel, runecraftXpGained, itemsReceived, itemsRemoved);
		manager.storeEvent(data);
		log.debug(data.toString());
	}

}