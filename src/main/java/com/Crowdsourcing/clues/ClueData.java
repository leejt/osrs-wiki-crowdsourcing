package com.Crowdsourcing.clues;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class ClueData
{
	private int itemId;
	private String text;
}
