/*
 * Copyright (c) 2022, andmcadams
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.Crowdsourcing.mahogany_homes;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import net.runelite.client.util.Text;

@Slf4j
public class CrowdsourcingMahoganyHomes {

    @Inject
    public Client client;

	@Inject
	public ClientThread clientThread;

	@Inject
	public CrowdsourcingManager manager;

	private boolean hasContract = false;
    private String lastContractorTalkedTo = null;
	private String currentContractor = null;
	private String currentHomeowner = null;
	private HashSet<Integer> currentJobTaskVarbs;

	private final HashMap<Integer, Integer> varbMap = new HashMap<>();

	private static final String CONTACT_STRING = "NPC Contact";
	private static final ImmutableSet<String> NPC_NAMES = ImmutableSet.<String>builder()
		.add("Amy").add("Angelo").add("Ellie").add("Marlo").build();

	public void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(this::updateVarbMap);
		}
	}

	public void shutDown()
	{
		varbMap.clear();
		hasContract = false;
		lastContractorTalkedTo = null;
		currentContractor = null;
		currentHomeowner = null;
		currentJobTaskVarbs = null;
	}

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
		// Temporarily hold the last contractor we think we talked to. This will be frozen in a new var when we get a task.
		if (menuOptionClicked.getMenuAction().equals(MenuAction.CC_OP))
		{
			if (Text.removeTags(menuOptionClicked.getMenuTarget()).equals(CONTACT_STRING))
			{
				lastContractorTalkedTo = "Amy";
				log.error("Stored npc: " + lastContractorTalkedTo);
			}
		}
		if (menuOptionClicked.getMenuAction().equals(MenuAction.NPC_FIRST_OPTION) ||
				menuOptionClicked.getMenuAction().equals(MenuAction.NPC_THIRD_OPTION))
		{
			String target = Text.removeTags(menuOptionClicked.getMenuTarget());

			if (NPC_NAMES.contains(target) && !target.equals(lastContractorTalkedTo))
			{
				lastContractorTalkedTo = target;
				log.error("Stored npc: " + lastContractorTalkedTo);
			}
		}
    }

	private boolean varbChanged;
	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Copied this pattern from stonedturtle, handle the changes in onGameTick to do it all at once.
		varbChanged = true;
	}

	private HashSet<Integer> updateVarbMap()
	{
		// Return the varbs that changed so we can tell how many tasks we have in a contract.
		// This could give more specific info, like which varbs changed and to what values
		HashSet<Integer> varbsChanged = new HashSet<>();
		for (final Hotspot spot : Hotspot.values())
		{
			int newValue = client.getVarbitValue(spot.getVarb());
			// If we have a contract and we see a non-zero change, we must be in the right spot.
			// TODO: Test this, this seems like the weakest link, though I think this works.
			// May not need hasContract, and that might actually not work in the edge case that you are in the area for an assignment when you get it (is hasContract set first or do the varb changes happen first?)
			if (hasContract && varbMap.get(spot.getVarb()) != newValue && newValue != 0)
			{
				varbsChanged.add(spot.getVarb());
			}
			varbMap.put(spot.getVarb(), newValue);
		}
		return varbsChanged;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (varbChanged)
		{
			varbChanged = false;
			HashSet<Integer> varbsChanged = updateVarbMap();
			if (currentContractor != null)
				if (currentJobTaskVarbs == null ||varbsChanged.size() >= currentJobTaskVarbs.size())
					currentJobTaskVarbs = varbsChanged;
		}
	}

	int getTierOfCompletedTask()
	{
		int tier = 0;
		// Values 5-8 are the tier of contract completed
		for (int val : varbMap.values())
		{
			tier = Math.max(tier, val);
		}

		// Normalizes tier from 5-8 to 1-4
		tier -= 4;
		if (tier < 0)
		{
			return 0;
		}

		return tier;
	}

	// Yoinked and adapted this function from https://github.com/TheStonedTurtle/Mahogany-Homes/blob/a9e118c1a07df4f4bd07e259ec23b6bef5f26206/src/main/java/thestonedturtle/mahoganyhomes/MahoganyHomesPlugin.java
	// Check for NPC dialog assigning or reminding us of a contract
	static final Pattern p = Pattern.compile("Go see <col=ff0000>(\\w*)</col>, .+");
	// Oddly enough, this actually does match only the MH messages (based on crowdsourcing messages)
	private static final Pattern CHAT_MESSAGE_CONTRACT_PATTERN = Pattern.compile("Go see <col=ff0000>(\\w*) ?</col>.+");
	private static final Pattern CONTRACT_FINISHED = Pattern.compile("You have completed [\\d,]* contracts with a total of [\\d,]* points?\\.");
	private static final ImmutableSet<String> HOMEOWNER_NAMES = ImmutableSet.<String>builder()
		.add("Jess").add("Noella").add("Ross").add("Larry").add("Norman").add("Tau").add("Barbara").add("Leela")
		.add("Mariah").add("Bob").add("Jeff").add("Sarah").build();

	@Subscribe
	public void onChatMessage(ChatMessage e)
	{
		if (!e.getType().equals(ChatMessageType.GAMEMESSAGE))
		{
			return;
		}

		// This is where we grab what homeowner you are currently helping.
		Matcher m = CHAT_MESSAGE_CONTRACT_PATTERN.matcher(e.getMessage());
		if (m.matches())
		{
			String homeownerName = m.group(1);
			if (HOMEOWNER_NAMES.contains(homeownerName))
			{
				log.info("Contract set for " + homeownerName);
				hasContract = true;
				currentContractor = lastContractorTalkedTo;
				currentHomeowner = homeownerName;
			}
			else
				log.error("Matched a contract message pattern but could not extract a valid name");
		}

		else if (CONTRACT_FINISHED.matcher(Text.removeTags(e.getMessage())).matches())
		{
			int tier = getTierOfCompletedTask();
			log.info("You just finished a tier " + tier + " contract, fixing " + currentJobTaskVarbs.toString() + " objects for " + currentHomeowner + " assigned by " + currentContractor);
			// This should give more info than just the number of tasks done
			MahoganyHomesData data = new MahoganyHomesData(currentContractor, currentHomeowner, tier, currentJobTaskVarbs);
			manager.storeEvent(data);
			log.info(data.toString());
			hasContract = false;
			lastContractorTalkedTo = null;
			currentContractor = null;
			currentHomeowner = null;
			currentJobTaskVarbs = null;
		}
	}

}
