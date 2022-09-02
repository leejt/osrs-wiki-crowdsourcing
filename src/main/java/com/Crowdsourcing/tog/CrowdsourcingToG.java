package com.Crowdsourcing.tog;


import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingToG
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private static final int TOG_REGION = 12948;

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		if (client.getLocalPlayer().getWorldLocation().getRegionID() != TOG_REGION)
		{
			return;
		}

		DecorativeObject object = event.getDecorativeObject();
		int id = object.getId();

		switch(id)
		{
			case ObjectID.BLUE_TEARS:
			case ObjectID.BLUE_TEARS_6665:
			case ObjectID.GREEN_TEARS:
			case ObjectID.GREEN_TEARS_6666:
				int world = client.getWorld();
				int gameTickToG = client.getTickCount();
				WorldPoint location = object.getWorldLocation();
				ToGData data = new ToGData(world, gameTickToG, id, location);
				manager.storeEvent(data);
				break;
		}
	}

}
