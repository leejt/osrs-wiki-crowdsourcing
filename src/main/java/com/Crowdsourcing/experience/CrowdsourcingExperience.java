package com.Crowdsourcing.experience;

import com.Crowdsourcing.CrowdsourcingManager;
import static com.google.common.base.MoreObjects.firstNonNull;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingExperience
{

	@Inject
	Client client;

	@Inject
	CrowdsourcingManager manager;

	private long lastAccountHash;
	private RuneScapeProfileType lastWorldType;
	private boolean initializeExperience;
	private MenuOptionClicked lastClick;


	private HashMap<String, Integer> skillExperience = new HashMap<>();

	public void startUp()
	{
		// Need this so when we start up the plugin after login we grab the current xp
		initializeExperience = true;
	}

	@Subscribe
	private void onStatChanged(StatChanged event)
	{
		// Ignore overall xp
		if (event.getSkill() == Skill.OVERALL)
			return;

		int lastExperience = skillExperience.get(event.getSkill().getName());
		skillExperience.put(event.getSkill().getName(), event.getXp());

		// Do not send initializing values since this is after world hop/login/turning on the plugin
		if (initializeExperience)
			return;

		if (client == null || client.getLocalPlayer() == null)
			return;

		int experienceDiff = event.getXp() - lastExperience;

		// Ignore things that are not experience changes
		if (experienceDiff == 0)
			return;

		log.debug("Stat change " + event.getSkill().getName() + " " + (event.getXp() - lastExperience));
		int currentLevel = client.getRealSkillLevel(event.getSkill());
		WorldPoint w = client.getLocalPlayer().getWorldLocation();
		boolean isInInstance = client.isInInstancedRegion();

		// Check map and if val is different, roll to store
		ExperienceData data = new ExperienceData(event.getSkill(), currentLevel, experienceDiff, w, isInInstance,
												 lastClick.getMenuAction(), lastClick.getId(), lastClick.getMenuOption(),
												 lastClick.getMenuTarget(), lastClick.getParam0(), lastClick.getParam1());
		manager.storeEvent(data);
	}

	private void resetState()
	{
		// Set all of the skills to either the current experience (if logged in) or 0 (if not)
		log.debug("Resetting state");
		skillExperience.clear();
		for (Skill s : Skill.values())
		{
			if (s == Skill.OVERALL)
				continue;
			if (client != null)
			{
				log.debug("Putting skill exp " + s.getName() + " " + client.getSkillExperience(s));
				skillExperience.put(s.getName(), client.getSkillExperience(s));
			}
			else
				skillExperience.put(s.getName(), 0);
		}
	}


	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Mostly taken from the ExpTrackerPlugin file. This checks to see if we need to reset the experience map due to
		// world type change or log out -> login with a new account.
		GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			// LOGGED_IN is triggered between region changes too.
			// Check that the username changed or the world type changed.
			RuneScapeProfileType type = RuneScapeProfileType.getCurrent(client);

			if (client.getAccountHash() != lastAccountHash || lastWorldType != type)
			{
				// Reset
				log.debug("World change: {} -> {}, {} -> {}",
					lastAccountHash, client.getAccountHash(),
					firstNonNull(lastWorldType, "<unknown>"),
					firstNonNull(type, "<unknown>"));

				lastAccountHash = client.getAccountHash();
				lastWorldType = type;
				resetState();
				// Must be set from hitting the LOGGING_IN or HOPPING case below
				assert initializeExperience;
			}
		}
		else if (state == GameState.LOGGING_IN || state == GameState.HOPPING)
		{
			initializeExperience = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		// Initialize (or re-initialize) experience if needed. This needs to be in game tick so we can check after login
		if (initializeExperience)
		{
			initializeExperience = false;
			resetState();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (menuOptionClicked.getMenuAction() != MenuAction.WALK
			&& !menuOptionClicked.getMenuOption().equals("Message"))
		{
			lastClick = menuOptionClicked;
		}
	}
}
