package com.Crowdsourcing.toa;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Skill;

@Data
@AllArgsConstructor
public class TombsSkillChangedData
{
	private final int gameTickToA;
	private final Skill skill;
	private final int baseLevel;
	private final int oldLevel;
	private final int newLevel;
}
