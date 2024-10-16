package com.Crowdsourcing.loot;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.http.api.loottracker.LootRecordType;

@Slf4j
public class CrowdsourcingLoot {
	@Inject
	Client client;

	@Inject
	ClientThread clientThread;

	@Inject
	CrowdsourcingManager manager;

	private static final Map<Integer, String> VARBITS_CA = new HashMap<>() {
		{
			put(12863, "easyCA");
			put(12864, "mediumCA");
			put(12865, "hardCA");
			put(12866, "eliteCA");
			put(12867, "masterCA");
			put(12868, "grandmasterCA");
		}
	};
	private static final int CA_CLAIMED = 2;

	private static final Map<Integer, String> VARBITS_CLUE_WARNINGS = new HashMap<>() {
		{
			put(10693, "beginnerClueWarn");
			put(10694, "easyClueWarn");
			put(10695, "mediumClueWarn");
			put(10723, "hardClueWarn");
			put(10724, "eliteClueWarn");
			put(10725, "masterClueWarn");
		}
	};
	private static final int CLUE_WARNING_ENABLED = 0;

	// Hunters' loot sacks
	private static final String HUNTERS_LOOT_SACK_BASIC = "Hunters' loot sack (basic)";
	private static final String HUNTERS_LOOT_SACK_ADEPT = "Hunters' loot sack (adept)";
	private static final String HUNTERS_LOOT_SACK_EXPERT = "Hunters' loot sack (expert)";
	private static final String HUNTERS_LOOT_SACK_MASTER = "Hunters' loot sack (master)";

	// Clues
	private static final Pattern CLUE_MESSAGE = Pattern.compile("You have a sneaking suspicion.*");

	// Rogue outfit
	private static final String ROGUE_MESSAGE = "Your rogue clothing allows you to steal twice as much loot!";

	private HashMap<String, Object> createSkillMap(Skill s)
	{
		HashMap<String, Object> h = new HashMap<>();
		h.put(s.getName(), client.getRealSkillLevel(s));
		h.put("B" + s.getName(), client.getBoostedSkillLevel(s));
		return h;
	}

	private int getRingOfWealth()
	{
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipmentContainer == null) {
			return 0;
		}

		if (equipmentContainer.contains(ItemID.RING_OF_WEALTH) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_I))
		{
			return 1;
		}

		if (equipmentContainer.contains(ItemID.RING_OF_WEALTH_1) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_2) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_3) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_4) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_5) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_I1) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_I2) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_I3) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_I4) ||
			equipmentContainer.contains(ItemID.RING_OF_WEALTH_I5))
		{
			return 2;
		}

		return 0;
	}

	private HashMap<String, Object> getMetadataForLoot(String name)
	{
		HashMap<String, Object> metadata = new HashMap<>();

		// universal metadata
		metadata.put("ringOfWealth", getRingOfWealth());  // 0=not wearing, 1=wearing uncharged, 2=wearing charged

		for (Map.Entry<Integer, String> entry : VARBITS_CA.entrySet())
		{
			int varbitId = entry.getKey();
			String caTier = entry.getValue();
			if (client.getVarbitValue(varbitId) == CA_CLAIMED)
			{
				metadata.put(caTier, true);
			}
		}

		for (Map.Entry<Integer, String> entry : VARBITS_CLUE_WARNINGS.entrySet())
		{
			int varbitId = entry.getKey();
			String clueTier = entry.getValue();
			if (client.getVarbitValue(varbitId) == CLUE_WARNING_ENABLED)
			{
				metadata.put(clueTier, true);
			}
		}

		if (name == null)
		{
			return metadata;
		}

		// conditional metadata
		switch (name)
		{
			case HUNTERS_LOOT_SACK_BASIC:
			case HUNTERS_LOOT_SACK_ADEPT:
			case HUNTERS_LOOT_SACK_EXPERT:
			case HUNTERS_LOOT_SACK_MASTER:
				metadata.putAll(createSkillMap(Skill.HERBLORE));
				metadata.putAll(createSkillMap(Skill.WOODCUTTING));
		}

		return metadata;
	}

	@Subscribe
	public void onLootReceived(LootReceived event)
	{
		String name = event.getName();
		int combatLevel = event.getCombatLevel();
		LootRecordType type = event.getType();

		LootData data = new LootData(name, combatLevel, type);

		for (ItemStack item: event.getItems())
		{
			int itemId = item.getId();
			int quantity = item.getQuantity();
			data.addItem(itemId, quantity);
		}

		data.setTick(client.getTickCount());
		data.setLocation(client.getLocalPlayer().getWorldLocation());

		clientThread.invokeLater(() -> {
			HashMap<String, Object> metadata = getMetadataForLoot(name);
			data.setMetadata(metadata);

//			log.info(data.toString());
			manager.storeEvent(data);
		});
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM)
		{
			return;
		}

		String message = event.getMessage();
		if (CLUE_MESSAGE.matcher(message).matches() || ROGUE_MESSAGE.equals(message))
		{
			LootData data = new LootData(null, -1, null);

			data.setMessage(message);
			data.setTick(client.getTickCount());
			data.setLocation(client.getLocalPlayer().getWorldLocation());

			clientThread.invokeLater(() -> {
				HashMap<String, Object> metadata = getMetadataForLoot(null);
				data.setMetadata(metadata);

//				log.info(data.toString());
				manager.storeEvent(data);
			});
		}
	}
}