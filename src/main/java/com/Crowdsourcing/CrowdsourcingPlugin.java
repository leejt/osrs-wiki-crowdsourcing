package com.Crowdsourcing;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

import com.Crowdsourcing.cooking.CrowdsourcingCooking;

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
	private static final int SECONDS_BETWEEN_UPLOADS = 10;

	@Inject
	private EventBus eventBus;

	@Inject
	CrowdsourcingManager manager;

	@Inject
	private CrowdsourcingCooking cooking;

	@Override
	protected void startUp() throws Exception
	{
		manager.storeEvent("hello!");
		eventBus.register(cooking);

	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(cooking);
	}

	@Schedule(
			period = SECONDS_BETWEEN_UPLOADS,
			unit = ChronoUnit.SECONDS,
			asynchronous = true
	)
	public void submitToAPI() {
		manager.submitToAPI();
	}
}
