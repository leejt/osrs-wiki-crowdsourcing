package com.Crowdsourcing.loot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.http.api.loottracker.LootRecordType;

@Data
@AllArgsConstructor
public class LootData
{
	private LootRecordType type;
	private String eventId;
	private List<Map<String, Integer>> drops;
	private int amount;
	private String message;
	private HashMap<String, Object> metadata;
}