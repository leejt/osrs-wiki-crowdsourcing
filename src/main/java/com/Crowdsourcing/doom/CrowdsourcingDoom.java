package com.Crowdsourcing.doom;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import com.Crowdsourcing.CrowdsourcingManager;
import net.runelite.api.Client;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;


public class CrowdsourcingDoom
{
    @Inject
    public CrowdsourcingManager manager;

    @Inject
    public Client client;

    private HashMap<Integer, Integer> prevDrops = new HashMap<>();
    private int prevDelve = 0;

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged)
    {
        int id = itemContainerChanged.getContainerId();

        if (id != InventoryID.DOM_LOOTPILE)
        {
            return;
        }

        String title = client.getWidget(InterfaceID.DomEndLevelUi.FRAME).getChild(1).getText(); // Level 2 Complete!
        int currDelve = Integer.parseInt(title.split(" ")[1]);

        if (currDelve != prevDelve + 1)
        {
            prevDrops = new HashMap<>();
            prevDelve = 0;
            return;
        }

        ItemContainer inv = itemContainerChanged.getItemContainer();

        HashMap<Integer, Integer> currDrops = new HashMap<>();
        for (Item item: inv.getItems())
        {
            currDrops.put(item.getId(), item.getQuantity());
        }

        ArrayList<DoomData> doomData = new ArrayList<>();
        for (int itemId : currDrops.keySet())
        {
            int itemQty = currDrops.get(itemId);
            if (!prevDrops.containsKey(itemId))
            {
                doomData.add(new DoomData(currDelve, itemId, itemQty));
            }
            else if (prevDrops.containsKey(itemId) && currDrops.get(itemId) > prevDrops.get(itemId))
            {
                int prevQty = prevDrops.get(itemId);
                int newQty = itemQty - prevQty;
                doomData.add(new DoomData(currDelve, itemId, newQty));
            }
        }

        if (doomData.isEmpty() ||
            (currDelve == 3 && doomData.size() > 2) ||
            (currDelve != 3 && doomData.size() > 1))
        {
            prevDrops = new HashMap<>();
            prevDelve = 0;
            return;
        }

        for (DoomData data : doomData)
        {
            manager.storeEvent(data);
        }

        prevDrops = currDrops;
        prevDelve = currDelve;
    }
}
