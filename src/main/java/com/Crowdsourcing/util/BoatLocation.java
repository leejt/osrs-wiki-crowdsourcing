package com.Crowdsourcing.util;

import net.runelite.api.Client;
import net.runelite.api.WorldEntity;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

public class BoatLocation
{
	public static WorldPoint fromLocal(Client client, LocalPoint local)
	{
		if (local == null)
		{
			return null;
		}

		WorldView wv = client.getLocalPlayer().getWorldView();
		int wvid = wv.getId();
		boolean isOnBoat = wvid != -1;
		if (isOnBoat)
		{
			WorldEntity we = client.getTopLevelWorldView().worldEntities().byIndex(wvid);
			return WorldPoint.fromLocalInstance(client, we.getLocalLocation());
		}
		return WorldPoint.fromLocalInstance(client, local);
	}
}