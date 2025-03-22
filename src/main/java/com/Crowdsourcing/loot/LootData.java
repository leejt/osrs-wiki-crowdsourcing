package com.Crowdsourcing.loot;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;
import net.runelite.http.api.loottracker.LootRecordType;

@Data
@AllArgsConstructor
public class LootData {
	private String name;
	private int combatLevel;
	private LootRecordType type;
	private ArrayList<HashMap<String, Integer>> items;
	private int amount;
	private String message;
	private WorldPoint location;
	private HashMap<String, Object> metadata;
	private int tick;
}