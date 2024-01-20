/*
 * Copyright (c) 2019, Weird Gloop <admin@weirdgloop.org>
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

package com.Crowdsourcing.sounds;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingAreaSoundEffects
{
	private AreaSoundEffectsData lastAreaSoundEffectsData = null;

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		/*
		The area sound effect played event only fires if the sound effect volume is >0%
		 */

		WorldPoint location = WorldPoint.fromScene(client, event.getSceneX(), event.getSceneY(), client.getPlane());

		Actor source = event.getSource();

		int soundId = event.getSoundId();
		int sourceId = (source instanceof NPC) ? ((NPC) source).getId() : -1;
		int animationId = (source != null) ? source.getAnimation() : -1;
		int x = location.getX();
		int y = location.getY();
		int z = client.getPlane();
		int range = event.getRange();
		int delay = event.getDelay();

		AreaSoundEffectsData data = new AreaSoundEffectsData(soundId, sourceId, animationId, x, y, z, range, delay);

		if (!data.equals(lastAreaSoundEffectsData))
		{
			manager.storeEvent(data);
		}

		lastAreaSoundEffectsData = data;
	}
}
