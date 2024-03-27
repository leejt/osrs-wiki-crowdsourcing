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
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.TileObject;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingSoundEffects
{
	private static final String NO_CHAT_MESSAGE = "";
	private static final int NO_SCENERY_ID = -1;

	private int soundEffectId = -1;
	private int soundEffectTick = -1;
	private int soundEffectDelay = -1;
	private int soundEffectSourceId = -1;
	private String chatMessage = NO_CHAT_MESSAGE;
	private int chatMessageTick = -1;
	private int menuOptionClickedId = -1;
	private int menuOptionClickedTick = -1;

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		checkSceneryProximity(event.getDecorativeObject(), 1, 1);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		// Example: a tree stump spawned after having chopped down a tree
		checkSceneryProximity(event.getGameObject(), event.getGameObject().sizeX(), event.getGameObject().sizeY());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		checkSceneryProximity(event.getGroundObject(), 1, 1);
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		// Example: Open/close door
		checkSceneryProximity(event.getWallObject(), 1, 1);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();

		if (!(ChatMessageType.SPAM.equals(type)
			|| ChatMessageType.GAMEMESSAGE.equals(type)
			|| ChatMessageType.CONSOLE.equals(type)
			|| ChatMessageType.DIALOG.equals(type)
			|| ChatMessageType.ENGINE.equals(type)
			|| ChatMessageType.MESBOX.equals(type)
			|| ChatMessageType.BROADCAST.equals(type)))
		{
			return;
		}

		chatMessage = event.getMessage();
		chatMessageTick = client.getTickCount();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (excludeMenuOptionClicked(event))
		{
			return;
		}

		menuOptionClickedTick = client.getTickCount();
		menuOptionClickedId = event.getId();

		// If the scenery is an imposter we use the imposter id instead
		ObjectComposition objectComposition = client.getObjectDefinition(menuOptionClickedId);
		if (objectComposition != null && objectComposition.getImpostorIds() != null && objectComposition.getImpostor() != null)
		{
			menuOptionClickedId = objectComposition.getImpostor().getId();
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		// The sound effect played event only fires if the sound effect volume is >0%
		Actor source = event.getSource();
		soundEffectTick = client.getTickCount();
		soundEffectId = event.getSoundId();
		soundEffectDelay = event.getDelay();
		soundEffectSourceId = source instanceof NPC ? ((NPC) source).getId() : -1;

		// Using invoke later to wait 1 tick to save the sound effect in case some scenery spawns later in the same tick
		clientThread.invokeLater(() -> saveSoundEffect(
			((soundEffectTick == menuOptionClickedTick) ||
			(soundEffectTick == (menuOptionClickedTick + 1))) ? menuOptionClickedId : NO_SCENERY_ID,
			chatMessageTick));
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

	private void checkSceneryProximity(TileObject scenery, int scenerySizeX, int scenerySizeY)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		int playerX = client.getLocalPlayer().getWorldLocation().getX();
		int playerY = client.getLocalPlayer().getWorldLocation().getY();
		int playerZ = client.getLocalPlayer().getWorldLocation().getPlane();

		int sceneryTickCount = client.getTickCount();

		int sceneryId = scenery.getId();
		int sceneryLocationX = scenery.getWorldLocation().getX();
		int sceneryLocationY = scenery.getWorldLocation().getY();
		int sceneryLocationZ = scenery.getWorldLocation().getPlane();

		if (playerZ != sceneryLocationZ)
		{
			return;
		}

		// If the scenery is an imposter we use the imposter id instead
		ObjectComposition objectComposition = client.getObjectDefinition(sceneryId);
		if (objectComposition != null && objectComposition.getImpostorIds() != null && objectComposition.getImpostor() != null)
		{
			sceneryId = objectComposition.getImpostor().getId();
		}

		// Check if the local player is on top of the scenery (don't think you can be on top of > 1x1 size scenery)
		if (distance(playerX, playerY, sceneryLocationX, sceneryLocationY) == 0)
		{
			saveSoundEffect(sceneryId, sceneryTickCount);
			return;
		}

		// Check if the local player is next to the scenery
		for (int dx = 0; dx < scenerySizeX; dx++)
		{
			if (distance(playerX, playerY, sceneryLocationX + dx, sceneryLocationY - 1) == 0 ||
				distance(playerX, playerY, sceneryLocationX + dx, sceneryLocationY + scenerySizeY) == 0)
			{
				saveSoundEffect(sceneryId, sceneryTickCount);
				return;
			}
		}
		for (int dy = 0; dy < scenerySizeY; dy++)
		{
			if (distance(playerX, playerY, sceneryLocationX - 1, sceneryLocationY + dy) == 0 ||
				distance(playerX, playerY, sceneryLocationX + scenerySizeX, sceneryLocationY + dy) == 0)
			{
				saveSoundEffect(sceneryId, sceneryTickCount);
				return;
			}
		}
	}

	private int distance(int x1, int y1, int x2, int y2)
	{
		return Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
	}

	private void saveSoundEffect(int sceneryId, int tick)
	{
		if (soundEffectTick <= 0)
		{
			// Ignore sound effects on the first tick of logging into the game
			return;
		}

		if (tick != soundEffectTick)
		{
			// Unable to associate the recently spawned scenery with the sound effect
			return;
		}

		SoundEffectsData data = new SoundEffectsData(
			soundEffectId,
			sceneryId,
			soundEffectSourceId,
			soundEffectDelay,
			chatMessage);
		manager.storeEvent(data);

		soundEffectId = -1;
		soundEffectTick = -1;
		soundEffectDelay = -1;
		soundEffectSourceId = -1;
		chatMessage = NO_CHAT_MESSAGE;
		chatMessageTick = -1;
		menuOptionClickedId = -1;
		menuOptionClickedTick = -1;
	}
}
