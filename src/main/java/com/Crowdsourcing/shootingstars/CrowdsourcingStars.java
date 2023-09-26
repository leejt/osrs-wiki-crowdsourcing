/*
 * Copyright (c) 2023, Ron Young <https://github.com/raiyni>
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
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

package com.Crowdsourcing.shootingstars;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NullNpcID;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.hiscore.HiscoreEndpoint;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
public class CrowdsourcingStars
{
	private static final String CROWDSOURCING_URL = "https://crowdsource.runescape.wiki/shooting_stars";
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
	private static final Pattern STAR_PROGRESS = Pattern.compile("This is a size-(?<tier>[0-9]+) star.* It has been mined (?<progress>[0-9]+)%.*", Pattern.CASE_INSENSITIVE);
	private static final int CHECK_SECONDS = 60 * 1000;
	private static final double HP_SUBMIT_CHANCE = 0.01;
	private static final double NEXT_TIER_SUBMIT_CHANCE = 0.01;
	private static final double MISSING_STAR_SUBMIT_CHANCE = 0.01;

	@Inject
	private Client client;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Gson gson;

	private Star trackedStar;
	private StarData lastSent;
	private WorldPoint lastLocation;

	private Map<Point, Long> checkedPoints = new HashMap<>();

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (event.getNpc().getId() != NullNpcID.NULL_10629)
		{
			return;
		}

		var npc = event.getNpc();
		if (trackedStar == null)
		{
			trackedStar = new Star(client.getWorld(), npc.getWorldLocation());
		}

		trackedStar.setNpc(npc);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc().getId() != NullNpcID.NULL_10629)
		{
			return;
		}

		if (trackedStar == null)
		{
			return;
		}

		trackedStar.setNpc(null);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		checkStarProgress();
		checkMissingStar();
		lastLocation = client.getLocalPlayer().getWorldLocation();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			reset();
		}

		// gameobjects don't fire despawn when teleporting away
		if (event.getGameState() == GameState.LOADING)
		{
			checkedPoints.clear();

			if (trackedStar != null)
			{
				trackedStar.setGameObject(null);
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		var gameObject = event.getGameObject();
		int tier = Tiers.of(gameObject.getId());
		if (tier < 0)
		{
			return;
		}

		if (trackedStar == null)
		{
			trackedStar = new Star(client.getWorld(), gameObject.getWorldLocation());
		}

		trackedStar.setGameObject(gameObject);

		// if for some reason someone runs away from the star
		// crossing a loading line next to the star can refire gameobject spawn events
		if (trackedStar.getTier() == tier)
		{
			return;
		}

		int lastTier = trackedStar.getTier();
		if (lastTier > -1)
		{
			trackedStar.setProgress(0);
		}

		trackedStar.setTier(tier);

		// only guaranteed submit found stars
		if (lastTier == -1 || shouldSubmit(NEXT_TIER_SUBMIT_CHANCE))
		{
			submit();
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		int tier = Tiers.of(event.getGameObject().getId());
		if (tier < 0)
		{
			return;
		}

		if (trackedStar != null)
		{
			trackedStar.setGameObject(null);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.DIALOG && event.getType() != ChatMessageType.MESBOX)
		{
			return;
		}

		Matcher m = STAR_PROGRESS.matcher(event.getMessage());
		if (!m.matches())
		{
			return;
		}

		if (trackedStar == null)
		{
			log.error("Tracked star not found");
			return;
		}

		int tier = MoreObjects.firstNonNull(Ints.tryParse(m.group("tier")), -1);
		int progress = MoreObjects.firstNonNull(Ints.tryParse(m.group("progress")), -1);

		trackedStar.setTier(tier);
		trackedStar.setProgress(progress);

		submit();
	}

	private boolean shouldSubmit(double chance)
	{
		double n = Math.random();
		return n >= 1d - chance;
	}

	private void submit()
	{
		if (trackedStar == null)
		{
			log.error("Missing star to submit");
			return;
		}

		if (lastSent != null &&
			lastSent.getTier() == trackedStar.getTier() &&
			(lastSent.getProgress() != null && (lastSent.getProgress() == trackedStar.getProgress() || trackedStar.getProgress() < lastSent.getProgress())) &&
			lastSent.getWorld() == trackedStar.getWorld() &&
			lastSent.getLocation().equals(trackedStar.getLocation()))
		{
			log.debug("Not submitting, same information");
			return;
		}

		lastSent = StarData.builder()
			.tier(trackedStar.getTier())
			.world(client.getWorld())
			.location(trackedStar.getLocation())
			.progress(trackedStar.getProgress() > -1 ? trackedStar.getProgress() : null)
			.mode(RuneScapeProfileType.getCurrent(client))
			.build();
		submitStar(lastSent);
	}

	private void submitStar(StarData data)
	{
		String json = gson.toJson(data);
		log.debug("submitting {}", json);

		Request r = new Request.Builder()
			.url(CROWDSOURCING_URL)
			.post(RequestBody.create(JSON, json))
			.build();

		okHttpClient.newCall(r).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.debug("Error sending crowdsourcing data", e);
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				log.debug("Successfully sent crowdsourcing data");
				response.close();
			}
		});
	}

	private void checkStarProgress()
	{
		if (trackedStar == null)
		{
			return;
		}

		var currentLocation = client.getLocalPlayer().getWorldLocation();
		if (lastLocation == null)
		{
			lastLocation = currentLocation;
		}

		if (trackedStar.getNpc() == null)
		{
			if (trackedStar.getGameObject() == null)
			{
				// star probably died, any tier can disappear if the next wave comes so rel
				if (trackedStar.getLocation().distanceTo2D(client.getLocalPlayer().getWorldLocation()) < 8 &&
					currentLocation.distanceTo2D(lastLocation) < 8)
				{
					trackedStar.setProgress(-1);
					trackedStar.setTier(0);

					submit();
				}

				reset();
			}

			return;
		}

		NPC npc = trackedStar.getNpc();
		int progress = 100 - 100 * npc.getHealthRatio() / npc.getHealthScale();
		if (progress == 0 || progress == trackedStar.getProgress())
		{
			return;
		}

		int lastProgress = trackedStar.getProgress();
		trackedStar.setProgress(progress);
		if (lastProgress == -1 || shouldSubmit(HP_SUBMIT_CHANCE))
		{
			submit();
		}
	}

	private void checkMissingStar()
	{
		if (trackedStar != null || client.getPlane() != 0)
		{
			return;
		}

		long now = System.currentTimeMillis();
		var currentLocation = client.getLocalPlayer().getWorldLocation();
		for (int regionId : client.getMapRegions())
		{
			if (!Locations.containsKey(regionId))
			{
				continue;
			}

			for (var p : Locations.get(regionId))
			{
				if (WorldPoint.isInScene(client, p.getX(), p.getY()) &&
					(Math.abs(currentLocation.getX() / 8 - p.getX() / 8) <= 3) && (Math.abs(currentLocation.getY() / 8 - p.getY() / 8) <= 3))
				{
					// always submit first seen missing star location
					long time = checkedPoints.getOrDefault(p, -1L);
					if (now - time < CHECK_SECONDS)
					{
						continue;
					}

					checkedPoints.put(p, now);
					if (time == -1 || shouldSubmit(MISSING_STAR_SUBMIT_CHANCE))
					{
						submitStar(StarData.builder()
							.tier(0)
							.world(client.getWorld())
							.location(new WorldPoint(p.getX(), p.getY(), client.getPlane()))
							.mode(RuneScapeProfileType.getCurrent(client))
							.build()
						);
					}
				}
			}
		}
	}

	public void reset()
	{
		lastSent = null;
		trackedStar = null;
		lastLocation = null;
		checkedPoints.clear();
	}
}
