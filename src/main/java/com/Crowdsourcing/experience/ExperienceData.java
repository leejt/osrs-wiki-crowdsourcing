package com.Crowdsourcing.experience;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;

@Data
@AllArgsConstructor
public class ExperienceData
{
	private Skill expSkill;
	private int expAmount;
	private WorldPoint expPoint;
	private MenuOptionClicked expLastOption;
}
