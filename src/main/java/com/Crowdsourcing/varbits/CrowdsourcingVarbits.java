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
import net.runelite.api.coords.LocalPoint;
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

		/* Blacklist certain common varbs that give us little useful data.
		 * 357 - Equipped weapon type
		 * 5983 - Dialogue option appear/disappear
		 * 8354 - 100 tick counter
		 */
		blackList = new HashSet<>();
		blackList.add(357);
		blackList.add(5983);
		blackList.add(8354);
		varbits = HashMultimap.create();

		if (client.getGameState() == GameState.STARTING || client.getGameState() == GameState.UNKNOWN)
		{
			return;
		}

		if (oldVarps == null)
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
			{
				return false;
			}
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

	private boolean disconnected = false;
	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState().equals(GameState.HOPPING)
			|| gameStateChanged.getGameState().equals(GameState.LOGGING_IN)
			|| (gameStateChanged.getGameState() == GameState.LOGGED_IN && disconnected))
		{
			disconnected = false;
			initializingTick = client.getTickCount() + 5;
			shutDown();
			startUp();
		}

		if (gameStateChanged.getGameState() == GameState.CONNECTION_LOST)
		{
			disconnected = true;
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
			if (oldValue != newValue && tick > initializingTick)
			{
				client.setVarbitValue(oldVarps2, i, newValue);
				if (!blackList.contains(i))
				{
					/* Wait a tick before grabbing location.
					 *
					 * This seems to cause fewer issues than not waiting a tick.
					 * Only noticeable issues seem to be in places like Dorgesh-Kaan, where you can go up and down
					 * stairs quickly to trick it into thinking varbs were updated in a different location.
					 * Without waiting a tick, certain loads/instances are messed up, including the Dorgesh-Kaan light
					 * varbs when loading a part of the map you haven't been to.
					 */
					clientThread.invokeLater(() ->
					{
						LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
						WorldPoint location = WorldPoint.fromLocalInstance(client, local);
						boolean isInInstance = client.isInInstancedRegion();

						VarData varbitData = new VarData(VARBIT, i, oldValue, newValue, tick, isInInstance, location);
						crowdsourcingManager.storeEvent(varbitData);
						// log.info(varbitData.toString());
					});
				}
			}
		}

		int oldValue = oldVarps2[index];
		int newValue = varps[index];

		// Push out varp changes
		if (oldValue != newValue && tick > initializingTick)
		{
			clientThread.invokeLater(() -> {
				LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
				WorldPoint location = WorldPoint.fromLocalInstance(client, local);
				boolean isInInstance = client.isInInstancedRegion();

				VarData varPlayerData = new VarData(VARPLAYER, index, oldValue, newValue, tick, isInInstance, location);
				crowdsourcingManager.storeEvent(varPlayerData);
				// log.info(varPlayerData.toString());
			});
		}

		oldVarps[index] = varps[index];
		oldVarps2[index] = varps[index];
	}

}
