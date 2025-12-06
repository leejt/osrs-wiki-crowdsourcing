package com.Crowdsourcing.loot;

import com.Crowdsourcing.util.BoatLocation;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.DBTableID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

public class LootMetadata
{
	private static final Map<Integer, String> VARBITS_CA = Map.of(
		VarbitID.CA_TIER_STATUS_EASY, "easy",
		VarbitID.CA_TIER_STATUS_MEDIUM, "medium",
		VarbitID.CA_TIER_STATUS_HARD, "hard",
		VarbitID.CA_TIER_STATUS_ELITE, "elite",
		VarbitID.CA_TIER_STATUS_MASTER, "master",
		VarbitID.CA_TIER_STATUS_GRANDMASTER, "grandmaster"
	);
	private static final int CA_CLAIMED = 2;

	private static final Map<Integer, String> VARBITS_CLUE_WARNINGS = Map.of(
		VarbitID.OPTION_TRAIL_REMINDER_BEGINNER, "beginner",
		VarbitID.OPTION_TRAIL_REMINDER_EASY, "easy",
		VarbitID.OPTION_TRAIL_REMINDER_MEDIUM, "medium",
		VarbitID.OPTION_TRAIL_REMINDER_HARD, "hard",
		VarbitID.OPTION_TRAIL_REMINDER_ELITE, "elite",
		VarbitID.OPTION_TRAIL_REMINDER_MASTER, "master"
	);
	private static final int CLUE_WARNING_ENABLED = 0;

	private static final Map<Integer, String> ROW_MAP = Map.ofEntries(
		Map.entry(ItemID.RING_OF_WEALTH, "uncharged"),
		Map.entry(ItemID.RING_OF_WEALTH_I, "uncharged (i)"),
		Map.entry(ItemID.RING_OF_WEALTH_1, "charged"),
		Map.entry(ItemID.RING_OF_WEALTH_2, "charged"),
		Map.entry(ItemID.RING_OF_WEALTH_3, "charged"),
		Map.entry(ItemID.RING_OF_WEALTH_4, "charged"),
		Map.entry(ItemID.RING_OF_WEALTH_5, "charged"),
		Map.entry(ItemID.RING_OF_WEALTH_I1, "charged (i)"),
		Map.entry(ItemID.RING_OF_WEALTH_I2, "charged (i)"),
		Map.entry(ItemID.RING_OF_WEALTH_I3, "charged (i)"),
		Map.entry(ItemID.RING_OF_WEALTH_I4, "charged (i)"),
		Map.entry(ItemID.RING_OF_WEALTH_I5, "charged (i)")
	);

	// https://oldschool.runescape.wiki/w/RuneScape:Varbit/4067
	private static final Map<Integer, String> SLAYER_MASTERS = Map.of(
		1, "Turael/Aya",
		2, "Mazchna/Achtryn",
		3, "Vannaka",
		4, "Chaeldar",
		5, "Duradel/Kuradal",
		6, "Nieve/Steve",
		7, "Krystilia",
		8, "Konar quo Maten",
		9, "Spria"
	);
	private static final int SLAYER_BOSS_TASK_ID = 98;

	private final Client client;

	private WorldPoint location;
	private int tick;
	private final Map<String, Boolean> combatAchievements = new HashMap<>();
	private final Map<String, Boolean> clueWarnings = new HashMap<>();
	private String ringOfWealth;
	private String slayerTask;
	private String slayerMaster;

	public LootMetadata(Client client)
	{
		this.client = client;

		setLocation();
		setTick();
		setCombatAchievements();
		setClueWarnings();
		setRingOfWealth();
		setSlayerTask();
	}

	private void setLocation()
	{
		LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
		if (local != null)
		{
			location = BoatLocation.fromLocal(client, local);
		}
	}

	private void setTick()
	{
		tick = client.getTickCount();
	}

	private void setCombatAchievements()
	{
		VARBITS_CA.forEach((varbitId, caTier) ->
			combatAchievements.put(caTier, client.getVarbitValue(varbitId) == CA_CLAIMED)
		);
	}

	private void setClueWarnings()
	{
		VARBITS_CLUE_WARNINGS.forEach((varbitId, clueTier) ->
			clueWarnings.put(clueTier, client.getVarbitValue(varbitId) == CLUE_WARNING_ENABLED)
		);
	}

	private void setRingOfWealth()
	{
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.WORN);
		if (equipmentContainer != null)
		{
			for (Map.Entry<Integer, String> entry : ROW_MAP.entrySet())
			{
				if (equipmentContainer.contains(entry.getKey()))
				{
					ringOfWealth = entry.getValue();
					return;
				}
			}
		}
	}

	private void setSlayerTask()
	{
		int slayerTaskQuantity = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
		if (slayerTaskQuantity > 0)
		{
			int taskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);

			int taskDBRow;
			if (taskId == SLAYER_BOSS_TASK_ID) /* from [proc,helper_slayer_current_assignment] */
			{
				var bossRows = client.getDBRowsByValue(
					DBTableID.SlayerTaskSublist.ID,
					DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID,
					0,
					client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID));

				if (bossRows.isEmpty())
				{
					return;
				}
				taskDBRow = (Integer) client.getDBTableField(bossRows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0)[0];
			}
			else
			{
				var taskRows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
				if (taskRows.isEmpty())
				{
					return;
				}
				taskDBRow = taskRows.get(0);
			}

			slayerTask = (String) client.getDBTableField(taskDBRow, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0)[0];
			slayerMaster = SLAYER_MASTERS.get(client.getVarbitValue(VarbitID.SLAYER_MASTER));
		}
	}

	public HashMap<String, Object> toMap()
	{
		return new HashMap<>() {{
			put("location", location);
			put("tick", tick);
			put("combatAchievements", combatAchievements);
			put("clueWarnings", clueWarnings);
			put("ringOfWealth", ringOfWealth);
			put("slayerTask", slayerTask);
			put("slayerMaster", slayerMaster);
		}};
	}
}