package com.Crowdsourcing.thieving;

import lombok.Data;
import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class PickpocketData
{
	private final int target;
	private final String message;
	private final WorldPoint location;
	private final boolean silence;
	private final boolean thievingCape;
	private final int ardougneDiary;
}