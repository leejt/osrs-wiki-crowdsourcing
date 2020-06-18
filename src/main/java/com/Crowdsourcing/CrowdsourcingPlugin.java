package com.Crowdsourcing;

import com.Crowdsourcing.dialogue.CrowdsourcingDialogue;
import com.Crowdsourcing.movement.CrowdsourcingMovement;
import com.Crowdsourcing.music.CrowdsourcingMusic;
import com.Crowdsourcing.woodcutting.CrowdsourcingWoodcutting;
import javax.inject.Inject;
import java.time.temporal.ChronoUnit;

import com.Crowdsourcing.cooking.CrowdsourcingCooking;
import lombok.extern.slf4j.Slf4j;

import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

@Slf4j
@PluginDescriptor(
	name = "OSRS Wiki Crowdsourcing",
	description = "Help figure out skilling success rates, burn rates, more. See osrs.wiki/RS:CROWD"
)
public class CrowdsourcingPlugin extends Plugin
{
	// Number of seconds to wait between trying to send data to the wiki.
	// NOTE: I wanted to make this a config entry but annotation parameters
	// need to be compile time constants.
	// I will either completely change the approach (stop using @Schedule) or
	// massively raise the time (~300 or 600 seconds) before making it widely
	// available. The current low value is for further testing from wiki editors.
	private static final int SECONDS_BETWEEN_UPLOADS = 60;

	@Inject
	private EventBus eventBus;

	@Inject
	CrowdsourcingManager manager;

	@Inject
	private CrowdsourcingCooking cooking;

	@Inject
	private CrowdsourcingDialogue dialogue;

	@Inject
	private CrowdsourcingMovement movement;

	@Inject
	private CrowdsourcingMusic music;

	@Inject
	private CrowdsourcingWoodcutting woodcutting;

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(cooking);
		eventBus.register(dialogue);
		eventBus.register(movement);
		eventBus.register(music);
		eventBus.register(woodcutting);
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(cooking);
		eventBus.unregister(dialogue);
		eventBus.unregister(movement);
		eventBus.unregister(music);
		eventBus.unregister(woodcutting);
	}

	@Schedule(
		period = SECONDS_BETWEEN_UPLOADS,
		unit = ChronoUnit.SECONDS,
		asynchronous = true
	)
	public void submitToAPI()
	{
		manager.submitToAPI();
	}
}
