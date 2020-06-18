package com.Crowdsourcing.music;

import lombok.Data;
import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class MusicUnlockData
{
	private final WorldPoint location;
	private final String message;
}