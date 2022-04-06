package com.Crowdsourcing.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Item;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class InventoryData
{
    private int id;
    private Item[] items;
    private WorldPoint location;
    private boolean isIronman;
}