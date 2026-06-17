package com.Crowdsourcing.toa;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import com.Crowdsourcing.CrowdsourcingManager;

public class CrowdsourcingTombs
{
	private static final String CHAT_MESSAGE_SMELLING_SALTS_START = "You crush the salts. Your heart rate increases.";
	private static final String CHAT_MESSAGE_NECTAR = "You drink some of the nectar. It hurts! This was not made for mortals.";
	private static final String CHAT_MESSAGE_TEARS = "You drink some of the tears.";
	private static final String CHAT_MESSAGE_TEARS_AOE = " has restored your prayer and combat stats.";
	private static final String CHAT_MESSAGE_AMBROSIA = "You drink the ambrosia. You feel reinvigorated.";
	private static final String CHAT_MESSAGE_HONEY_LOCUST = "You bite down on the dried bug. It's very chewy, but you feel slightly reinvigorated...";
	private static final int REGION_HET_PUZZLE = 14674;
	private static final int VARBIT_BOSS_HEALTH = 6099;
	private static final int VARBIT_BOSS_MAX_HEALTH = 6100;
	private static final String NAME_HETS_SEAL = "<col=00ffff>Het's Seal (weakened)</col>";

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private int currentTick = -1;
	private int miningTick = -1;
	private boolean lethalHit = false;
	private Pickaxe pickaxe = null;

	private final ImmutableSet<Skill> skillsToCheck = ImmutableSet.<Skill>builder()
		.add(Skill.HITPOINTS, Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
			Skill.RANGED, Skill.MAGIC, Skill.PRAYER, Skill.RANGED).build();

	//Map of boosted combat skills prior to stat change: {Skill, prevBoostedSkill}
	private final HashMap<Skill, Integer> prevCombatSkills = new HashMap<>();

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		String message = chatMessage.getMessage();

		boolean teammateUsedTears = message.contains(CHAT_MESSAGE_TEARS_AOE);
		boolean playerUsedItemOnSelf = (message.equals(CHAT_MESSAGE_TEARS) || message.equals(CHAT_MESSAGE_NECTAR) ||
			message.equals(CHAT_MESSAGE_AMBROSIA) || message.equals(CHAT_MESSAGE_HONEY_LOCUST) ||
			message.equals(CHAT_MESSAGE_SMELLING_SALTS_START));
		if (teammateUsedTears || playerUsedItemOnSelf)
		{
			// Take a note of what our skills are prior to them changing (onChatMessage fires before onStatChanged)
			for (Skill s : skillsToCheck)
			{
				prevCombatSkills.put(s, client.getBoostedSkillLevel(s));
			}
			currentTick = client.getTickCount();
			TombsChatMessageData data = new TombsChatMessageData(currentTick, message);
			manager.storeEvent(data);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		Skill skill = statChanged.getSkill();

		if (skill == Skill.MINING && isInHetPuzzle())
		{
			miningTick = client.getTickCount();
			Player player = client.getLocalPlayer();
			pickaxe = Pickaxe.fromAnimation(player.getAnimation());
		}

		if (currentTick == client.getTickCount() && skillsToCheck.contains(skill))
		{
			// If we see the old and new levels are the same, assume this is an xp change and exit
			if (client.getBoostedSkillLevel(skill) == prevCombatSkills.get(skill))
			{
				return;
			}

			TombsSkillChangedData data = new TombsSkillChangedData(
				currentTick,
				skill,
				client.getRealSkillLevel(skill),
				prevCombatSkills.get(skill),
				client.getBoostedSkillLevel(skill)
			);
			manager.storeEvent(data);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (!isInHetPuzzle())
		{
			return;
		}

		if (varbitChanged.getValue() == 0) //only care when seal dies
		{
			int id = varbitChanged.getVarbitId();
			if (id == VARBIT_BOSS_HEALTH) //set flag to ignore final blow hits against seal
			{
				lethalHit = true;
			}
			else if (id == VARBIT_BOSS_MAX_HEALTH) //reset flag in case of multiple raid runs in same session
			{
				lethalHit = false;
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (!isInHetPuzzle() && client.getTickCount() != miningTick + 1)
		{
			return;
		}

		String targetName = hitsplatApplied.getActor().getName();
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		int damage = hitsplat.getAmount();
		if (targetName != null && targetName.equals(NAME_HETS_SEAL) && hitsplat.isMine() && !lethalHit)
		{
			final boolean isMaxHit = hitsplat.getHitsplatType() == HitsplatID.DAMAGE_MAX_ME;
			TombsMiningData data = new TombsMiningData(
				pickaxe,
				client.getRealSkillLevel(Skill.MINING),
				client.getBoostedSkillLevel(Skill.MINING),
				damage,
				isMaxHit
			);
			manager.storeEvent(data);
		}
	}

	private boolean isInHetPuzzle()
	{
		Player player = client.getLocalPlayer();
		int instanceId = WorldPoint.fromLocalInstance(client, player.getLocalLocation()).getRegionID();
		return instanceId == REGION_HET_PUZZLE;
	}
}
