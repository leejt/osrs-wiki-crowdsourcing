package com.Crowdsourcing.varbits;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.IndexDataBase;
import net.runelite.api.VarbitComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingVarbits
{

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager crowdsourcingManager;

	@Inject
	private ClientThread clientThread;

	private static final int VARBITS_ARCHIVE_ID = 14;

	private int[] oldVarps = null;
	private Multimap<Integer, Integer> varbits;

	private int initializingTick = 0;

	public void startUp()
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

	public void shutDown()
	{
		varbits = null;
		oldVarps = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.equals(GameState.HOPPING) || gameStateChanged.equals(GameState.LOGGING_IN))
		{
			initializingTick = client.getTickCount();
			shutDown();
			startUp();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		int tick = client.getTickCount();

		// Whenever a varbit is changed, record it and pass the info off to be submitted.
		int index = varbitChanged.getIndex();
		int[] varps = client.getVarps();

		for (int i : varbits.get(index))
		{
			int oldValue = client.getVarbitValue(oldVarps, i);
			int newValue = client.getVarbitValue(varps, i);

			// If the varbit is being changed on an initializing tick (when logging in),
			// don't push out varbit changes. There are too many, and are generally uninteresting.
			if (oldValue != newValue && tick != initializingTick)
			{
					// We should probably ignore the name, since there aren't many, and they can easily be found
					// in post analysis.
					WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
					VarbitData varbitData = new VarbitData(i, oldValue, newValue, tick, playerLocation);
					log.info(varbitData.toString());
					// crowdsourcingManager.storeEvent(varbitData);
			}
		}

		System.arraycopy(client.getVarps(), 0, oldVarps, 0, oldVarps.length);
	}

}
