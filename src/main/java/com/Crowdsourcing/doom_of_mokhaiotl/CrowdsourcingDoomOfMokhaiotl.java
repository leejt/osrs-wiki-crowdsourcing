package com.Crowdsourcing.doom_of_mokhaiotl;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import javax.inject.Inject;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingDoomOfMokhaiotl
{
	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;

	Map<Integer, Map<Integer, Integer>> lootByWave = new HashMap<>();

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
	{
		int id = itemContainerChanged.getContainerId();

		if (id != InventoryID.DOM_LOOTPILE)
		{
			return;
		}
		String title = client.getWidget(InterfaceID.DomEndLevelUi.FRAME).getChild(1).getText();
		int currDelve = Integer.parseInt(title.split(" ")[1]);

		ItemContainer inv = itemContainerChanged.getItemContainer();

		// All loot is everything that is offered to the player
		HashMap<Integer, Integer> allLoot = new HashMap<>();
		for (Item item : inv.getItems())
		{
			allLoot.put(item.getId(), item.getQuantity());
		}

		// Check if the same loot already exists for this delve
		if (allLoot.equals(lootByWave.get(currDelve)))
		{
			return;
		}

		lootByWave.put(currDelve, allLoot);

		manager.storeEvent(new DomLootData(lootByWave));
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if(chatMessage.getType() == ChatMessageType.GAMEMESSAGE && chatMessage.getMessage().contains("Delve level: 1"))
		{
			lootByWave.clear();
		}
	}
}
