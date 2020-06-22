package com.Crowdsourcing.movement;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
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
	private MenuOptionClicked lastClick;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (menuOptionClicked.getMenuAction() != MenuAction.WALK
			&& !menuOptionClicked.getMenuOption().equals("Message"))
		{
			lastClick = menuOptionClicked;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
		WorldPoint nextPoint = WorldPoint.fromLocalInstance(client, local);
		boolean nextIsInInstance = client.isInInstancedRegion();
		if (lastPoint != null)
		{
			int distance = nextPoint.distanceTo(lastPoint);
			if (distance > 2 || nextIsInInstance != lastIsInInstance)
			{
				MovementData data = new MovementData(lastPoint, nextPoint, lastIsInInstance, nextIsInInstance, ticksStill, lastClick);
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
