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
	private static final String CHAT_MESSAGE_NECTAR = "You drink some of the nectar. It hurts! This was not made for mortals.";
	private static final String CHAT_MESSAGE_TEARS = "You drink some of the tears.";
	private static final String CHAT_MESSAGE_TEARS_AOE = " has restored your prayer and combat stats.";
	private static final String CHAT_MESSAGE_AMBROSIA = "You drink the ambrosia. You feel reinvigorated.";
	private static final String CHAT_MESSAGE_HONEY_LOCUST = "You bite down on the dried bug. It's very chewy, but you feel slightly reinvigorated...";

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private int gametickToA = -1;

	//Map of boosted combat skills prior to stat change: {Skill, prevBoostedSkill}
	private HashMap<Skill, Integer> prevCombatSkills = new HashMap<>();

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

			prevCombatSkills.put(Skill.HITPOINTS, client.getBoostedSkillLevel(Skill.HITPOINTS));
			prevCombatSkills.put(Skill.ATTACK, client.getBoostedSkillLevel(Skill.ATTACK));
			prevCombatSkills.put(Skill.STRENGTH, client.getBoostedSkillLevel(Skill.STRENGTH));
			prevCombatSkills.put(Skill.DEFENCE, client.getBoostedSkillLevel(Skill.DEFENCE));
			prevCombatSkills.put(Skill.RANGED, client.getBoostedSkillLevel(Skill.RANGED));
			prevCombatSkills.put(Skill.MAGIC, client.getBoostedSkillLevel(Skill.MAGIC));
			prevCombatSkills.put(Skill.PRAYER, client.getBoostedSkillLevel(Skill.PRAYER));

			ToAChatMessageData data = new ToAChatMessageData(gametickToA, message);
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
				case ATTACK:
				case STRENGTH:
				case DEFENCE:
				case RANGED:
				case MAGIC:
				case PRAYER:
					ToASkillChangedData data = new ToASkillChangedData(
						gametickToA,
						skill,
						client.getRealSkillLevel(skill),
						prevCombatSkills.get(skill),
						client.getBoostedSkillLevel(skill)
					);
					manager.storeEvent(data);
					break;
			}
		}
	}

}
