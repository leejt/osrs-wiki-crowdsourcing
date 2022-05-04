package com.Crowdsourcing.mahogany_homes;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
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

    private static int lastAction = -1;
	private boolean hasContract = false;
    private String lastContractorTalkedTo = null;
	private String currentContractor = null;
	private String currentHomeowner = null;
	private int currentJobTasks = 0;

	private final HashMap<Integer, Integer> varbMap = new HashMap<>();
    private final String[] strs = {"Expert Contract (Requires 70 Construction)"};

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
		currentJobTasks = 0;
	}

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
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
		varbChanged = true;
	}

	private int updateVarbMap()
	{
		int numChanged = 0;
		for (final Hotspot spot : Hotspot.values())
		{
			int newValue = client.getVarbitValue(spot.getVarb());
			// If we have a contract and we see a non-zero change, we must be in the right spot.
			if (hasContract && varbMap.get(spot.getVarb()) != newValue && newValue != 0)
				numChanged += 1;
			varbMap.put(spot.getVarb(), newValue);
		}
		return numChanged;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (varbChanged)
		{
			varbChanged = false;
			int numChanged = updateVarbMap();
			if (currentContractor != null)
				currentJobTasks = Math.max(currentJobTasks, numChanged);
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
			log.info("You just finished a tier " + tier + " contract, fixing " + currentJobTasks + " objects for " + currentHomeowner + " assigned by " + currentContractor);
			MahoganyHomesData data = new MahoganyHomesData(currentContractor, currentHomeowner, tier, currentJobTasks);

			hasContract = false;
			lastContractorTalkedTo = null;
			currentContractor = null;
			currentHomeowner = null;
			currentJobTasks = 0;
		}
	}

}
