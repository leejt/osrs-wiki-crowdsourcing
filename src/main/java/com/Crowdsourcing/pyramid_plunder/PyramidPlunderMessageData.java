package com.Crowdsourcing.pyramid_plunder;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class PyramidPlunderMessageData
{
	private String ppMessageText;
	private WorldPoint ppLocation;
	private int ppThievingLevel;
	private int ppBoostedThievingLevel;
	private int ppTick;
}
