package com.Crowdsourcing.movement;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingMovement
{
	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	private WorldPoint lastPoint;
	private int ticksStill;
	private boolean lastIsInInstance;

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		WorldPoint nextPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		boolean nextIsInInstance = client.isInInstancedRegion();
		if (lastPoint != null)
		{
			int distance = nextPoint.distanceTo(lastPoint);
			if (distance > 2 || nextIsInInstance != lastIsInInstance)
			{
				MovementData data = new MovementData(lastPoint, nextPoint, lastIsInInstance, nextIsInInstance, ticksStill);
				manager.storeEvent(data);
			}
			if (distance > 0)
			{
				ticksStill = 0;
			}
		}
		ticksStill++;
		lastPoint = nextPoint;
		lastIsInInstance = nextIsInInstance;
	}
}