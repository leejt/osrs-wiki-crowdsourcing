package com.Crowdsourcing.loot;

import com.Crowdsourcing.util.BoatLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

public class LootMetadata
{
	private static final int CA_CLAIMED = 2;
	private static final int CLUE_WARNING_ENABLED = 0;

	private static final Set<Integer> EQUIPMENT_WHITELIST = Set.of(
		ItemID.RING_OF_WEALTH,
		ItemID.RING_OF_WEALTH_I,
		ItemID.RING_OF_WEALTH_1,
		ItemID.RING_OF_WEALTH_2,
		ItemID.RING_OF_WEALTH_3,
		ItemID.RING_OF_WEALTH_4,
		ItemID.RING_OF_WEALTH_5,
		ItemID.RING_OF_WEALTH_I1,
		ItemID.RING_OF_WEALTH_I2,
		ItemID.RING_OF_WEALTH_I3,
		ItemID.RING_OF_WEALTH_I4,
		ItemID.RING_OF_WEALTH_I5
	);

	private static Map<String, Integer> getLocation(Client client)
	{
		Map<String, Integer> location = new HashMap<>();

		LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
		WorldPoint boatLocation = BoatLocation.fromLocal(client, local);
		if (boatLocation != null)
		{
			location.put("x", boatLocation.getX());
			location.put("y", boatLocation.getY());
			location.put("plane", boatLocation.getPlane());
		}
		return location;
	}

	private static int getTick(Client client)
	{
		return client.getTickCount();
	}

	private static Map<String, Boolean> getCombatAchievements(Client client)
	{
		Function<Integer, Boolean> isClaimed = id -> client.getVarbitValue(id) == CA_CLAIMED;
		return Map.of(
			"easy", isClaimed.apply(VarbitID.CA_TIER_STATUS_EASY),
			"medium", isClaimed.apply(VarbitID.CA_TIER_STATUS_MEDIUM),
			"hard", isClaimed.apply(VarbitID.CA_TIER_STATUS_HARD),
			"elite", isClaimed.apply(VarbitID.CA_TIER_STATUS_ELITE),
			"master", isClaimed.apply(VarbitID.CA_TIER_STATUS_MASTER),
			"grandmaster", isClaimed.apply(VarbitID.CA_TIER_STATUS_GRANDMASTER)
		);
	}

	private static Map<String, Boolean> getClueWarnings(Client client)
	{
		Function<Integer, Boolean> isEnabled = id -> client.getVarbitValue(id) == CLUE_WARNING_ENABLED;
		return Map.of(
			"beginner", isEnabled.apply(VarbitID.OPTION_TRAIL_REMINDER_BEGINNER),
			"easy", isEnabled.apply(VarbitID.OPTION_TRAIL_REMINDER_EASY),
			"medium", isEnabled.apply(VarbitID.OPTION_TRAIL_REMINDER_MEDIUM),
			"hard", isEnabled.apply(VarbitID.OPTION_TRAIL_REMINDER_HARD),
			"elite", isEnabled.apply(VarbitID.OPTION_TRAIL_REMINDER_ELITE),
			"master", isEnabled.apply(VarbitID.OPTION_TRAIL_REMINDER_MASTER)
		);
	}

	private static List<Integer> getWornItems(Client client)
	{
		List<Integer> wornItems = new ArrayList<>();

		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.WORN);
		if (equipmentContainer != null)
		{
			for (Item item : equipmentContainer.getItems())
			{
				int itemId = item.getId();
				if (EQUIPMENT_WHITELIST.contains(itemId))
				{
					wornItems.add(itemId);
				}
			}
		}
		return wornItems;
	}

	// column 0 on https://abextm.github.io/cache2/#/viewer/dbtable/113
	private static int getSlayerTaskID(Client client)
	{
		return client.getVarpValue(VarPlayerID.SLAYER_TARGET);
	}

	// column 1 on https://abextm.github.io/cache2/#/viewer/dbtable/116
	// corresponds to slayerTaskID == 98
	private static int getSlayerBossTaskID(Client client)
	{
		return client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID);
	}

	// Not to be confused with VarPlayerID.SLAYER_COUNT_ORIGINAL
	private static int getSlayerTaskRemainingCount(Client client)
	{
		return client.getVarpValue(VarPlayerID.SLAYER_COUNT);
	}

	// see: https://oldschool.runescape.wiki/w/RuneScape:Varbit/4067
	private static int getSlayerMasterID(Client client)
	{
		int slayerMasterID = -1;

		// Player currently has task assigned iff slayer_count > 0.
		if (client.getVarpValue(VarPlayerID.SLAYER_COUNT) > 0)
		{
			slayerMasterID = client.getVarbitValue(VarbitID.SLAYER_MASTER);
		}
		return slayerMasterID;
	}

	private static List<String> getWorldTypes(Client client)
	{
		List<String> worldTypes = new ArrayList<>();
		for (WorldType wt : client.getWorldType())
		{
			worldTypes.add(wt.toString());
		}
		if (!worldTypes.contains("MEMBERS"))
		{
			worldTypes.add("FREE");
		}
		return worldTypes;
	}

	private static int getWorldNumber(Client client)
	{
		return client.getWorld();
	}

	private static List<Integer> getPortTaskIDs(Client client)
	{
		return List.of(
			client.getVarbitValue(VarbitID.PORT_TASK_SLOT_0_ID),
			client.getVarbitValue(VarbitID.PORT_TASK_SLOT_1_ID),
			client.getVarbitValue(VarbitID.PORT_TASK_SLOT_2_ID),
			client.getVarbitValue(VarbitID.PORT_TASK_SLOT_3_ID),
			client.getVarbitValue(VarbitID.PORT_TASK_SLOT_4_ID)
		);
	}

	private static List<Integer> getPortTaskCounts(Client client)
	{
		return List.of(
			client.getVarbitValue(VarbitID.BOUNTY_TASK_ITEM_COUNTER_1),
			client.getVarbitValue(VarbitID.BOUNTY_TASK_ITEM_COUNTER_2),
			client.getVarbitValue(VarbitID.BOUNTY_TASK_ITEM_COUNTER_3),
			client.getVarbitValue(VarbitID.BOUNTY_TASK_ITEM_COUNTER_4),
			client.getVarbitValue(VarbitID.BOUNTY_TASK_ITEM_COUNTER_5)
		);
	}

	public static HashMap<String, Object> getMap(Client client, Object lootTrackerMetadata)
	{
		return new HashMap<>()
		{{
			put("location", getLocation(client));
			put("tick", getTick(client));
			put("combatAchievements", getCombatAchievements(client));
			put("clueWarnings", getClueWarnings(client));
			put("wornItems", getWornItems(client));
			put("slayerTaskID", getSlayerTaskID(client));
			put("slayerBossTaskID", getSlayerBossTaskID(client));
			put("slayerTaskRemainingCount", getSlayerTaskRemainingCount(client));
			put("slayerMasterID", getSlayerMasterID(client));
			put("worldTypes", getWorldTypes(client));
			put("worldNumber", getWorldNumber(client));
			put("lootTrackerMetadata", lootTrackerMetadata != null ? lootTrackerMetadata : -1);
			put("portTaskIDs", getPortTaskIDs(client));
			put("portTaskCounts", getPortTaskCounts(client));
		}};
	}
}