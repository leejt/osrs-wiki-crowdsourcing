package com.Crowdsourcing.port_tasks;

import java.util.Deque;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class PortTaskData
{
	private final WorldPoint playerLocation;
	private final int sailingLevel;
	private final Integer noticeBoard;
	private final int[] currentTasks;
	private final List<Integer> offeredTasks;
	private final Deque<Integer> lastTasks;
}
