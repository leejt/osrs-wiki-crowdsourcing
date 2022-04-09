package com.Crowdsourcing.pyramid_plunder;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class PyramidPlunderSceneryData
{
	private int ppSceneryId;
	private int ppBaseId;
	private int ppMenuAction;
	private WorldPoint ppLocation;
	private int ppThievingLevel;
	private int ppBoostedThievingLevel;
}
