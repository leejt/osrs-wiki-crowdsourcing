package com.Crowdsourcing.inventory;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
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

    private static final ImmutableSet<Integer> blacklist = ImmutableSet.of(
        InventoryID.INVENTORY.getId(), InventoryID.BANK.getId(), InventoryID.EQUIPMENT.getId(),
        InventoryID.TRADE.getId(), InventoryID.TRADEOTHER.getId()
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
