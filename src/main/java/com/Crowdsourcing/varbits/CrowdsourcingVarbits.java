/*
 * Copyright (c) 2018 Abex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.Crowdsourcing.varbits;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
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
	private int[] oldVarps2 = null;
	private Multimap<Integer, Integer> varbits;

	private int initializingTick = 0;

	private static HashSet<Integer> blackList;

	private static final int VARBIT = 0;
	private static final int VARPLAYER = 1;

	public void startUp()
	{

		blackList = new HashSet<>();
		blackList.add(357);
		blackList.add(5983);
		blackList.add(8354);
		varbits = HashMultimap.create();

		if(oldVarps == null)
		{
			oldVarps = new int[client.getVarps().length];
			oldVarps2 = new int[client.getVarps().length];
		}

		// Set oldVarps to be the current varps
		System.arraycopy(client.getVarps(), 0, oldVarps, 0, oldVarps.length);
		System.arraycopy(client.getVarps(), 0, oldVarps2, 0, oldVarps2.length);

		// For all varbits, add their ids to the multimap with the varp index as their key
		clientThread.invoke(() -> {
			if (client.getIndexConfig() == null)
				return false;
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
			return true;
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
		if (gameStateChanged.getGameState().equals(GameState.HOPPING)
			|| gameStateChanged.getGameState().equals(GameState.LOGGING_IN))
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
				client.setVarbitValue(oldVarps2, i, newValue);
				if (!blackList.contains(i))
				{
					WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
					VarData varbitData = new VarData(VARBIT, i, oldValue, newValue, tick, playerLocation);
					log.info(varbitData.toString());
					crowdsourcingManager.storeEvent(varbitData);
				}
			}
		}

		int oldValue = oldVarps2[index];
		int newValue = varps[index];

		if (oldValue != newValue && tick != initializingTick)
		{
			WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
			VarData varPlayerData = new VarData(VARPLAYER, index, oldValue, newValue, tick, playerLocation);
			log.info(varPlayerData.toString());
			crowdsourcingManager.storeEvent(varPlayerData);
		}

		oldVarps[index] = varps[index];
		oldVarps2[index] = varps[index];
	}

}
