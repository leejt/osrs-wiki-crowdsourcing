package com.Crowdsourcing.doom_of_mokhaiotl;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import javax.inject.Inject;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingDoomOfMokhaiotl
{
	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;

	HashMap<Integer, Integer> allLoot = new HashMap<>();
	HashMap<Integer, Integer> prevLoot = new HashMap<>();
	List<Map<Integer, Integer>> waveLoot = new ArrayList<>();

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
		// from the sum of all completed delves
		allLoot.clear();
		for (Item item: inv.getItems())
		{
			allLoot.put(item.getId(), item.getQuantity());
		}

		// Previous loot is empty or all loot from the previous delve completion
		// We determine which item stacks are different which is the loot being
		// offered to the player on this floor
		Map<Integer, Integer> deltaLoot = new HashMap<>();
		for (Map.Entry<Integer, Integer> entry : allLoot.entrySet())
		{
			int itemId = entry.getKey();
			int totalQty = entry.getValue();
			int oldQty = prevLoot.getOrDefault(itemId, 0);

			if (totalQty - oldQty > 0)
			{
				deltaLoot.put(itemId, totalQty - oldQty);
			}
		}

		// Loot for each delve is stored in an arraylist with the index being
		// one less than the delve level
		while (waveLoot.size() < currDelve)
		{
			waveLoot.add(new HashMap<>());
		}

		waveLoot.set(currDelve - 1, deltaLoot);

		prevLoot.clear();
		prevLoot.putAll(allLoot);
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		// When the player claims their loot we submit the loot
		if(event.getScriptId() != ScriptID.DOM_LOOT_CLAIM)
		{
			return;
		}
		storeData();
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		// If the player dies before claiming their loot it is also submitted
		Actor actor = actorDeath.getActor();
		if (actor instanceof Player)
		{
			Player player = (Player) actor;
			if (player == client.getLocalPlayer())
			{
				storeData();
			}
		}
	}

	public void storeData()
	{
		if (waveLoot != null && !waveLoot.isEmpty())
		{
			manager.storeEvent(waveLoot);
		}
		waveLoot = new ArrayList<>();
		allLoot.clear();
		prevLoot.clear();
	}
}
