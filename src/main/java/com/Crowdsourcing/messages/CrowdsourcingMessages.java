package com.Crowdsourcing.messages;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingMessages
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		String message = chatMessage.getMessage();
		if (client == null || client.getLocalPlayer() == null)
		{
			return;
		}
		LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
		if (local == null)
		{
			return;
		}
		WorldPoint location = WorldPoint.fromLocalInstance(client, local);
		boolean isInInstance = client.isInInstancedRegion();
		MessagesData data = new MessagesData(message, isInInstance, location);
		log.debug("Storing data: " + message);
		manager.storeEvent(data);
	}
}
