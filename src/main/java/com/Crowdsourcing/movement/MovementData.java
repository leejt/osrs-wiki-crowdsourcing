package com.Crowdsourcing.movement;

import lombok.Data;
import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class MovementData
{
	private final WorldPoint start;
	private final WorldPoint end;
	private final int ticks;
}