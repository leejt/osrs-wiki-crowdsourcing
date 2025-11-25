package com.Crowdsourcing.port_tasks;

import java.util.Deque;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PortTaskData
{
	private final int sailingLevel;
	private final int noticeBoard;
	private final int[] currentTasks;
	private final List<Integer> offeredTasks;
	private final Deque<Integer> lastTasks;
}
