package com.Crowdsourcing.loot;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.loottracker.LootReceived;

@Slf4j
public class CrowdsourcingLoot
{
	@Inject
	Client client;

	@Inject
	ClientThread clientThread;

	@Inject
	CrowdsourcingManager manager;

	// Clues
	private static final Pattern CLUE_WARNING_MESSAGE = Pattern.compile("You have a sneaking suspicion.*");

	// Rogue outfit
	private static final String ROGUE_MESSAGE = "Your rogue clothing allows you to steal twice as much loot!";

	// Fishing/mining standard messages
	// "You catch a swordfish.", "You catch some shrimps.", "You catch a shark!", "You catch a scroll box!"
	private static final Pattern FISHING_PATTERN = Pattern.compile("You catch .*");
	private static final Pattern MINING_PATTERN = Pattern.compile("You manage to mine some .*");
	private static final String MINING_CLUE_MESSAGE = "You find a scroll box!";

	@Subscribe
	public void onLootReceived(LootReceived event)
	{
		List<Map<String, Integer>> drops = new ArrayList<>();
		event.getItems().forEach(item ->
			drops.add(Map.of(
				"id", item.getId(),
				"qty", item.getQuantity()
			))
		);

		clientThread.invokeLater(() ->
			manager.storeEvent(new LootData(
				event.getType(),
				event.getName(),
				drops,
				event.getAmount(),
				null,
				(new LootMetadata(client)).toMap()
			))
		);
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
		if (CLUE_WARNING_MESSAGE.matcher(message).matches() ||
			ROGUE_MESSAGE.equals(message) ||
			FISHING_PATTERN.matcher(message).matches() ||
			MINING_PATTERN.matcher(message).matches() ||
			MINING_CLUE_MESSAGE.equals(message))
		{
			clientThread.invokeLater(() ->
				manager.storeEvent(new LootData(
					null,
					"message",
					null,
					0,
					message,
					(new LootMetadata(client)).toMap()
				))
			);
		}
	}
}