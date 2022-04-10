package com.Crowdsourcing.pyramid_plunder;

import com.Crowdsourcing.CrowdsourcingManager;
import com.Crowdsourcing.messages.MessagesData;
import com.Crowdsourcing.overhead_dialogue.CrowdsourcingOverheadDialogue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import static net.runelite.api.MenuAction.GAME_OBJECT_FIRST_OPTION;
import static net.runelite.api.MenuAction.GAME_OBJECT_SECOND_OPTION;
import static net.runelite.api.MenuAction.ITEM_FIRST_OPTION;
import static net.runelite.api.MenuAction.NPC_THIRD_OPTION;
import net.runelite.api.NPCComposition;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Skill;
import net.runelite.api.World;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingPyramidPlunder
{

	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;


	ImmutableSet<Integer> varbsToTrack = ImmutableSet.<Integer>builder()
		// Urns
		.add(2346).add(2347).add(2348).add(2349).add(2350).add(2351).add(2352).add(2353).add(2354).add(2355)
		.add(2356).add(2357).add(2358).add(2359)
		// Sarcophagus
		.add(2362)
		// Grand gold chest
		.add(2363).build();

	HashMap<Integer, Integer> varbVals = new HashMap<>();

	public void startUp()
	{
		if (client == null)
			return;

		for (Integer varbIndex : varbsToTrack)
		{
			if (varbIndex == null)
				continue;
			varbVals.put(varbIndex, 0);
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (client == null)
			return;
		// Check each varb
		for (Integer varbIndex : varbsToTrack)
		{
			// If the varb has changed, update the value in the map and create a new data object to log
			int oldVarbVal = varbVals.get(varbIndex);
			int newVarbVal = client.getVarbitValue(varbIndex);
			if (newVarbVal != oldVarbVal)
			{
				// Create varb data
				varbVals.put(varbIndex, client.getVarbitValue(varbIndex));
				WorldPoint w = client.getLocalPlayer().getWorldLocation();
				int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
				int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
				PyramidPlunderVarbData data = new PyramidPlunderVarbData(varbIndex, oldVarbVal, newVarbVal, w, unboostedLevel, boostedLevel);
				log.error(data.toString());
				// manager.storeEvent(data);
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		String message = chatMessage.getMessage();
		if (client == null || client.getLocalPlayer() == null)
		{
			return;
		}
		int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
		int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
		WorldPoint w = client.getLocalPlayer().getWorldLocation();
		WorldArea inside = new WorldArea(1916, 4418, 70, 62, 0);
		WorldArea insideFloor1 = new WorldArea(1916, 4418, 70, 62, 1);
		WorldArea insideFloor2 = new WorldArea(1916, 4418, 70, 62, 2);
		WorldArea outside = new WorldArea(3281, 2787, 16, 17, 0);
		if (!w.isInArea(inside) && !w.isInArea(insideFloor1) && !w.isInArea(insideFloor2) && !w.isInArea(outside))
		{
			return;
		}
		PyramidPlunderMessageData data = new PyramidPlunderMessageData(message, w, unboostedLevel, boostedLevel);
		log.error(data.toString());
		// manager.storeEvent(data);
	}

	// 20956, 20974, 20975, 20976, 20977, 20978, 20979, 20987, 21251, 21252, 21253, 21254 (outside doors)
	// 21280 (Speartrap)
	// 20946, 20947 (Grand gold chest)
	// 21255, 21256, 21257 (Sarcophagus) Could probably just track first (unopened)
	// 21261 - 21279 Urns galore
	// 20948, 20949(inside doors)
	// 20931, 20932 (inside doors)
	ImmutableSet<Integer> objectIdsToCheck = ImmutableSet.<Integer>builder()
		.add(20956).add(20974).add(20975).add(20976).add(20977).add(20978).add(20979)
		.add(20987).add(21251).add(21252) .add(21253).add(21254)
		.add(21280).add(20946).add(20947).add(21255).add(21256).add(21257)
		.add(21261).add(21262).add(21263).add(21264).add(21265).add(21266).add(21267).add(21268).add(21269)
		.add(21270).add(21271).add(21272).add(21273).add(21274).add(21275).add(21276).add(21277).add(21278).add(21279)
		.add(20948).add(20949)
		.add(20931).add(20932).build();
	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event) {
		if (event.getMenuAction() == GAME_OBJECT_FIRST_OPTION || event.getMenuAction() == GAME_OBJECT_SECOND_OPTION)
		{
			ObjectComposition objectComposition = client.getObjectDefinition(event.getId());
			// If imposter ids is null, use the composition's id (loc is not a multiloc in this case)
			int id;
			if (objectComposition.getImpostorIds() == null)
				id = objectComposition.getId();
			else
				id = objectComposition.getImpostor().getId();
			if (objectIdsToCheck.contains(id))
			{
				int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
				int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
				WorldPoint w = client.getLocalPlayer().getWorldLocation();
				PyramidPlunderSceneryData data = new PyramidPlunderSceneryData(id, objectComposition.getId(), event.getMenuAction().getId(), w, unboostedLevel, boostedLevel);
				log.error(data.toString());
			}
		}
		// Guardian mummy event
		else if (event.getMenuAction() == NPC_THIRD_OPTION)
		{
			log.error(event.getMenuTarget());
			if (!event.getMenuTarget().equals("<col=ffff00>Guardian mummy"))
				return;
			int unboostedLevel = client.getRealSkillLevel(Skill.THIEVING);
			int boostedLevel = client.getBoostedSkillLevel(Skill.THIEVING);
			WorldPoint w = client.getLocalPlayer().getWorldLocation();
			PyramidPlunderSceneryData data = new PyramidPlunderSceneryData(1779, 1779, event.getMenuAction().getId(), w, unboostedLevel, boostedLevel);
			log.error(data.toString());
		}
	}

}
