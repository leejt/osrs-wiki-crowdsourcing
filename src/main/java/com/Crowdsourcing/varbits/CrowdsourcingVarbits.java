package com.Crowdsourcing.varbits;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.IndexDataBase;
import net.runelite.api.VarbitComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingVarbits
{

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager crowdsourcingManager;

	@Inject
	private ClientThread clientThread;

	private static final int VARBITS_ARCHIVE_ID = 14;

	private int tick = 0;
	private int[] oldVarps = null;
	private Multimap<Integer, Integer> varbits;

	private void initialize()
	{
		varbits = HashMultimap.create();

		if(oldVarps == null)
			oldVarps = new int[client.getVarps().length];

		// Set oldVarps to be the current varps
		System.arraycopy(client.getVarps(), 0, oldVarps, 0, oldVarps.length);

		// For all varbits, add their ids to the multimap with the varp index as their key
		clientThread.invoke(() -> {
			IndexDataBase indexVarbits = client.getIndexConfig();
			final int[] varbitIds = indexVarbits.getFileIds(VARBITS_ARCHIVE_ID);
			for (int id : varbitIds)
			{
				VarbitComposition varbit = client.getVarbit(id);
				if (varbit != null)
				{
					varbits.put(varbit.getIndex(), id);
				}
			}
		});
	}

	private void teardown()
	{
		varbits = null;
		oldVarps = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		// Due to the number of varbits loaded on login (approx 2k+), we want to avoid capturing
		// the first few ticks on login.

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		// Whenever a varbit is changed, record it and pass the info off to be submitted.
		int tick = client.getTickCount();

		int index = varbitChanged.getIndex();
		int[] varps = client.getVarps();

		for (int i : varbits.get(index))
		{
			int oldValue = client.getVarbitValue(oldVarps, i);
			int newValue = client.getVarbitValue(varps, i);

			if (oldValue != newValue)
			{
				// We should probably ignore the name, since there aren't many, and they can easily be found
				// in post analysis.
				WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
				VarbitData varbitData = new VarbitData(i, oldValue, newValue, tick, playerLocation);
				crowdsourcingManager.storeEvent(varbitData);
			}
		}

		System.arraycopy(client.getVarps(), 0, oldVarps, 0, oldVarps.length);
	}

}
