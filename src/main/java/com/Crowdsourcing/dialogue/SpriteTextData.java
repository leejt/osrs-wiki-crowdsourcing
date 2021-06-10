package com.Crowdsourcing.dialogue;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class SpriteTextData
{
	private String text;
	private int itemId;
	private final boolean isInInstance;
	private final WorldPoint location;
}
