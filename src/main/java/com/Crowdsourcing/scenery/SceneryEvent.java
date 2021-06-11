package com.Crowdsourcing.scenery;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class SceneryEvent
{
	private final SceneryEventType type;
	private final WorldPoint location;
	private final int id;
}