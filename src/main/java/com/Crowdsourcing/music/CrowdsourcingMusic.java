package com.Crowdsourcing.music;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingMusic
{

	private static final Pattern MUSIC_UNLOCK_PATTERN = Pattern.compile("You have unlocked a new music track:.*");

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE)
		{
			String message = event.getMessage();
			if (MUSIC_UNLOCK_PATTERN.matcher(message).matches())
			{
				WorldPoint location = client.getLocalPlayer().getWorldLocation();
				MusicUnlockData data = new MusicUnlockData(location, message);
				manager.storeEvent(data);
			}
		}
	}
}
