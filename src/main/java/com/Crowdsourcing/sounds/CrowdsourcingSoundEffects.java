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
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingSoundEffects
{
	private int sceneryTickCount;
	private int sceneryId;
	private String sceneryMenuAction;

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (excludeMenuOptionClicked(event))
		{
			return;
		}

		sceneryTickCount = client.getTickCount();
		sceneryId = event.getId();
		sceneryMenuAction = event.getMenuAction().toString();

		// If the scenery is an imposter we use the imposter id instead
		ObjectComposition objectComposition = client.getObjectDefinition(event.getId());
		if (objectComposition != null && objectComposition.getImpostorIds() != null && objectComposition.getImpostor() != null)
		{
			sceneryId = objectComposition.getImpostor().getId();
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		/*
		The sound effect played event only fires if the sound effect volume is >0%
		 */

		int tickCount = client.getTickCount();

		/*
		The sound looks to be playing the same tick or sometimes the next tick after having interacted with the scenery,
		but including the next tick might be more prone to incorrectly mapping to the wrong sound effect.
		Being attacked by an NPC will trigger sound effects that often play the same tick as sound effects triggered
		by scenery, so there will be bad data that needs to be filtered out later.
		 */
		if (tickCount == sceneryTickCount || tickCount == (sceneryTickCount + 1))
		{
			int soundEffectId = event.getSoundId(); 
			SoundEffectsData data = new SoundEffectsData(sceneryMenuAction, sceneryId, soundEffectId);
			manager.storeEvent(data);
		}
	}

	private boolean excludeMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		MenuAction action = menuOptionClicked.getMenuAction();

		/*
		Quickly clicking "Cancel" in the inventory or "Walk here" on the
		ground after having performed an action that played a sound effect
		would make it look as if the last action was the source of the sound
		*/
		return MenuAction.CANCEL.equals(action)
			|| MenuAction.WALK.equals(action) // Walking by clicking a tile through "Walk here"
			|| MenuAction.CC_OP.equals(action) // Inventory tabs, orbs and buttons, e.g. "Combat Options", "Quick-prayers"
			|| MenuAction.CC_OP_LOW_PRIORITY.equals(action) // Inventory items
			|| MenuAction.EXAMINE_NPC.equals(action)
			|| MenuAction.EXAMINE_OBJECT.equals(action)
			|| MenuAction.EXAMINE_ITEM_GROUND.equals(action)
			|| MenuAction.RUNELITE.equals(action)
			|| MenuAction.RUNELITE_OVERLAY.equals(action)
			|| MenuAction.RUNELITE_HIGH_PRIORITY.equals(action)
			|| MenuAction.RUNELITE_INFOBOX.equals(action)
			|| MenuAction.RUNELITE_OVERLAY_CONFIG.equals(action)
			|| MenuAction.RUNELITE_PLAYER.equals(action)
			|| MenuAction.RUNELITE_SUBMENU.equals(action)
			;
	}
}
