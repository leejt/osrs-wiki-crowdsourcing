package com.Crowdsourcing.tog;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class ToGData
{
	private final int world;
	private final int gameTickToG;
	private final int objectId;
	private final WorldPoint spawnedLocation;
}
