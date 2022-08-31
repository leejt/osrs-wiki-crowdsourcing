package com.Crowdsourcing.toa;

import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.crowdsourcing.CrowdsourcingManager;

public class CrowdsourcingToA
{
	private static final String CHAT_MESSAGE_SMELLING_SALTS_START = "You crush the salts. Your heart rate increases.";
	private static final String CHAT_MESSAGE_SMELLING_SALTS_END = "The boost from the smelling salts has worn off!"; //may not be needed
	private static final String CHAT_MESSAGE_NECTAR = "You drink some of the nectar. It hurts! This was not made for mortals.";
	private static final String CHAT_MESSAGE_TEARS = "You drink some of the tears.";
	private static final String CHAT_MESSAGE_TEARS_AOE = " has restored your prayer and combat stats.";
	private static final String CHAT_MESSAGE_AMBROSIA = "You drink the ambrosia. You feel reinvigorated.";
	private static final String CHAT_MESSAGE_HONEY_LOCUST = "You bite down on the dried bug. Its very chewy, but you feel slightly reinvigorated...";
	private static final String BAD_DATA = "BAD DATA";

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private int gametickToA = -1;
	private String dataString = "";

	//Map of boosted combat skills prior to stat change: {SKILL, prevBoostedSkill}
	private HashMap<String, Integer> prevCombatSkills = new HashMap<>();

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String message = chatMessage.getMessage();

		if (message.contains(CHAT_MESSAGE_TEARS_AOE) ||
			(message.equals(CHAT_MESSAGE_TEARS) || message.equals(CHAT_MESSAGE_NECTAR) ||
				message.equals(CHAT_MESSAGE_AMBROSIA) || message.equals(CHAT_MESSAGE_HONEY_LOCUST) ||
				message.equals(CHAT_MESSAGE_SMELLING_SALTS_START)))
		{
			gametickToA = client.getTickCount();

			prevCombatSkills.put("HITPOINTS", client.getBoostedSkillLevel(Skill.HITPOINTS));
			prevCombatSkills.put("ATTACK", client.getBoostedSkillLevel(Skill.ATTACK));
			prevCombatSkills.put("STRENGTH", client.getBoostedSkillLevel(Skill.STRENGTH));
			prevCombatSkills.put("DEFENCE", client.getBoostedSkillLevel(Skill.DEFENCE));
			prevCombatSkills.put("RANGED", client.getBoostedSkillLevel(Skill.RANGED));
			prevCombatSkills.put("MAGIC", client.getBoostedSkillLevel(Skill.MAGIC));
			prevCombatSkills.put("PRAYER", client.getBoostedSkillLevel(Skill.PRAYER));

			dataString = "{tick=" + gametickToA + ", message=" + message + "}";
			ToAData data = new ToAData(dataString);
			manager.storeEvent(data);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		Skill skill = statChanged.getSkill();

		if (gametickToA == client.getTickCount())
		{
			switch(skill)
			{
				case HITPOINTS:
					dataString="{tick=" + gametickToA + ", skill=HITPOINTS, baseLevel=" +
						client.getRealSkillLevel(Skill.HITPOINTS) + ", oldLevel=" + prevCombatSkills.get("HITPOINTS") +
						", newLevel=" + client.getBoostedSkillLevel(Skill.HITPOINTS) + "}";
					break;
				case ATTACK:
					dataString="{tick=" + gametickToA + ", skill=ATTACK, baseLevel=" +
						client.getRealSkillLevel(Skill.ATTACK) + ", oldLevel=" + prevCombatSkills.get("ATTACK") +
						", newLevel=" + client.getBoostedSkillLevel(Skill.ATTACK) + "}";
					break;
				case STRENGTH:
					dataString="{tick=" + gametickToA + ", skill=STRENGTH, baseLevel=" +
						client.getRealSkillLevel(Skill.STRENGTH) + ", oldLevel=" + prevCombatSkills.get("STRENGTH") +
						", newLevel=" + client.getBoostedSkillLevel(Skill.STRENGTH) + "}";
					break;
				case DEFENCE:
					dataString="{tick=" + gametickToA + ", skill=DEFENCE, baseLevel=" +
						client.getRealSkillLevel(Skill.DEFENCE) + ", oldLevel=" + prevCombatSkills.get("DEFENCE") +
						", newLevel=" + client.getBoostedSkillLevel(Skill.DEFENCE) + "}";
					break;
				case RANGED:
					dataString="{tick=" + gametickToA + ", skill=RANGED, baseLevel=" +
						client.getRealSkillLevel(Skill.HITPOINTS) + ", oldLevel=" + prevCombatSkills.get("RANGED") +
						", newLevel=" + client.getBoostedSkillLevel(Skill.RANGED) + "}";
					break;
				case MAGIC:
					dataString="{tick=" + gametickToA + ", skill=MAGIC, baseLevel=" +
						client.getRealSkillLevel(Skill.MAGIC) + ", oldLevel=" + prevCombatSkills.get("MAGIC") +
						", newLevel=" + client.getBoostedSkillLevel(Skill.MAGIC) + "}";
					break;
				case PRAYER:
					dataString="{tick=" + gametickToA + ", skill=PRAYER, baseLevel=" +
						client.getRealSkillLevel(Skill.PRAYER) + ", oldLevel=" + prevCombatSkills.get("PRAYER") +
						", newLevel=" + client.getBoostedSkillLevel(Skill.PRAYER) + "}";
					break;
				default:
					dataString = BAD_DATA;
			}

			if (!dataString.equals(BAD_DATA))
			{
				ToAData data = new ToAData(dataString);
				manager.storeEvent(data);
			}
		}
	}

}
