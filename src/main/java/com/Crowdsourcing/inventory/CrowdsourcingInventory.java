package com.Crowdsourcing.inventory;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingInventory
{

    @Inject
    public CrowdsourcingManager manager;

    @Inject
    public Client client;

	// There's no gameval equivalent to the old InventoryID.TRADEOTHER so this is effectively the old value
	private static final int TRADE_OTHER_INVENTORY_ID = (InventoryID.TRADEOFFER | 0x8000);

    private static final ImmutableSet<Integer> blacklist = ImmutableSet.of(
		InventoryID.INV, InventoryID.BANK, InventoryID.WORN,
		InventoryID.TRADEOFFER, TRADE_OTHER_INVENTORY_ID
    );

    // Cache seen inventories so we avoid sending them more than once
    private static HashSet<Integer> seenInventories = new HashSet<>();

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
    {
        int id = itemContainerChanged.getContainerId();

        if (blacklist.contains(id) || seenInventories.contains(id))
        {
            return;
        }

        seenInventories.add(id);

        Item[] items = itemContainerChanged.getItemContainer().getItems().clone();

        if (client == null || client.getLocalPlayer() == null)
        {
            return;
        }
        LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
        if (local == null)
        {
            return;
        }
        WorldPoint location = WorldPoint.fromLocalInstance(client, local);

        manager.storeEvent(new InventoryData(id, items, location, client.getAccountType().isIronman()));
    }
}
