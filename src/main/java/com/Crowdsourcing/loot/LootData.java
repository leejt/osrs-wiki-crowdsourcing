package com.Crowdsourcing.loot;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;
import net.runelite.http.api.loottracker.LootRecordType;

@Data
public class LootData {
	private String name;
	private int combatLevel;
	private LootRecordType type;
	private ArrayList<HashMap<String, Integer>> items;
	private String message;
	private WorldPoint location;
	private HashMap<String, Object> metadata;
	private int tick;

	public LootData(String name, int combatLevel, LootRecordType type)
	{
		this.name = name;
		this.combatLevel = combatLevel;
		this.type = type;
		this.items = null;
		this.message = null;
		this.location = null;
		this.metadata = null;
		this.tick = -1;
	}

	public void addItem(int itemId, int quantity)
	{
		if (this.items == null)
		{
			this.items = new ArrayList<>();
		}

		HashMap<String, Integer> drop = new HashMap<>() {
			{
				put("id", itemId);
				put("qty", quantity);
			}
		};
		this.items.add(drop);
	}
}