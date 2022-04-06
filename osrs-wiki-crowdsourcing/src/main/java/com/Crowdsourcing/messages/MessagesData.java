package com.Crowdsourcing.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class MessagesData
{
	private String message;
	private boolean isInInstance;
	private WorldPoint location;
}
