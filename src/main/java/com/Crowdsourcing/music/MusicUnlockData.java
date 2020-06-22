package com.Crowdsourcing.music;

import lombok.Data;
import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class MusicUnlockData
{
	private final WorldPoint location;
	private final boolean isInInstance;
	private final String message;
}