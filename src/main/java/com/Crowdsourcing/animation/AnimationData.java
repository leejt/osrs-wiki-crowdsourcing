package com.Crowdsourcing.animation;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class AnimationData
{
	private int animationId;
	private int npcId;
	private boolean isPlayer;
	private boolean isInInstance;
	private WorldPoint location;
}
