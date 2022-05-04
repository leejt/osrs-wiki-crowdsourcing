package com.Crowdsourcing.experience;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;

@Data
@AllArgsConstructor
public class ExperienceData
{
	private Skill expSkill;
	private int expAmount;
	private int expCurrentLevel;
	private WorldPoint expPoint;
	private boolean expIsInInstance;
	private MenuAction expMenuAction;
	private int expMenuIdentifier;
	private String expMenuOption;
	private String expMenuTarget;
	private int expParam0;
	private int expParam1;
}
