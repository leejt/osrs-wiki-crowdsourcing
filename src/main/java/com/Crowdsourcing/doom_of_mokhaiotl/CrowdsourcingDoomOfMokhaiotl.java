package com.Crowdsourcing.doom_of_mokhaiotl;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import javax.inject.Inject;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
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

	List<Map<Integer, Integer>> lootByWave = new ArrayList<>();

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
		for (Item item: inv.getItems())
		{
			allLoot.put(item.getId(), item.getQuantity());
		}

		while (lootByWave.size() < currDelve)
		{
			lootByWave.add(new HashMap<>());
		}

		lootByWave.set(currDelve - 1, allLoot);

		manager.storeEvent(new DomLootData(lootByWave));
	}

}
