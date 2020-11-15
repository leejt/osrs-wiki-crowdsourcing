package com.Crowdsourcing;

import com.Crowdsourcing.varbits.CrowdsourcingVarbits;
import javax.inject.Inject;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

@Slf4j
@PluginDescriptor(
	name = "OSRS Wiki Crowdsourcing (advanced)",
	description = "Help figure out varbits, quest states, and more. See osrs.wiki/RS:CROWD"
)
public class AdvancedCrowdsourcingPlugin extends Plugin
{
	// Number of seconds to wait between trying to send data to the wiki.
	// NOTE: I wanted to make this a config entry but annotation parameters
	// need to be compile time constants.
	private static final int SECONDS_BETWEEN_UPLOADS = 300;

	@Inject
	private EventBus eventBus;

	@Inject
	CrowdsourcingManager manager;

	@Inject
	private CrowdsourcingVarbits varbits;

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(varbits);

		varbits.startUp();
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(varbits);

		varbits.shutDown();
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
