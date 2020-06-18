package com.Crowdsourcing.woodcutting;

import com.Crowdsourcing.skilling.SkillingEndReason;
import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class WoodcuttingData
{
	private final int level;
	private final int startTick;
	private final int endTick;
	private final List<Integer> chopTicks;
	private final List<Integer> nestTicks;
	private final int axe;
	private final int treeId;
	private final WorldPoint treeLocation;
	private final SkillingEndReason reason;
}
