/*
 * Copyright (c) 2021, Patrick <https://github.com/pwatts6060>
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

package com.Crowdsourcing.mining;

import com.Crowdsourcing.skilling.SkillingEndReason;
import com.Crowdsourcing.skilling.SkillingState;
import com.google.common.collect.ImmutableMap;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.runelite.client.plugins.crowdsourcing.CrowdsourcingManager;

public class CrowdsourcingMining
{
	private static final String SWING_MESSAGE = "You swing your pick at the rock.";
	private static final String INVENTORY_FULL_MESSAGE = "Your inventory is too full to hold";
	private static final String GEM_MESSAGE = "You just found";
	private static final Map<Integer, RockType> ORE_OBJECTS = new ImmutableMap.Builder<Integer, RockType>().
			put(ObjectID.ROCKS_11378, RockType.BLURITE).
			put(ObjectID.ROCKS_11379, RockType.BLURITE).
			put(ObjectID.ROCKS_11161, RockType.COPPER).
			put(ObjectID.ROCKS_10943, RockType.COPPER).
			put(ObjectID.ROCKS_11361, RockType.TIN).
			put(ObjectID.ROCKS_11360, RockType.TIN).
			put(ObjectID.ROCKS_11366, RockType.COAL).
			put(ObjectID.ROCKS_11367, RockType.COAL).
			put(ObjectID.ROCKS_11372, RockType.MITHRIL).
			put(ObjectID.ROCKS_11373, RockType.MITHRIL).
			put(ObjectID.ROCKS_11374, RockType.ADAMANT).
			put(ObjectID.ROCKS_11375, RockType.ADAMANT).
			put(ObjectID.ROCKS_11362, RockType.CLAY).
			put(ObjectID.ROCKS_11363, RockType.CLAY).
			put(ObjectID.ROCKS_11364, RockType.IRON).
			put(ObjectID.ROCKS_11365, RockType.IRON).
			put(ObjectID.ROCKS_11368, RockType.SILVER).
			put(ObjectID.ROCKS_11369, RockType.SILVER).
			put(ObjectID.ROCKS_11370, RockType.GOLD).
			put(ObjectID.ROCKS_11371, RockType.GOLD).
			put(ObjectID.ROCKS_11386, RockType.SANDSTONE).
			put(ObjectID.ROCKS_11387, RockType.GRANITE).
			put(ObjectID.ROCKS_11380, RockType.GEMROCK).
			put(ObjectID.ROCKS_11381, RockType.GEMROCK).
			put(ObjectID.PILE_OF_ROCK, RockType.LIMESTONE).
			put(ObjectID.CRYSTALS, RockType.AMETHYST).
			put(ObjectID.CRYSTALS_11389, RockType.AMETHYST).
			put(ObjectID.ROCKS_11377, RockType.RUNITE).
			put(ObjectID.ROCKS_11376, RockType.RUNITE).
			build();

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private SkillingState state = SkillingState.RECOVERING;
	private int coolDown;
	private int lastExperimentEnd = 0;
	private WorldPoint rockLocation;
	private int rockId;
	private RockType rockType;
	private int startTick;
	private Pickaxe pickaxe;
	private int amulet;
	private boolean ranToRock;
	private WorldPoint startPoint;
	private List<Integer> oreTicks = new ArrayList<>();
	private List<Integer> gemTicks = new ArrayList<>();

	private void endExperiment(SkillingEndReason reason)
	{
		if (state == SkillingState.MINING)
		{
			int endTick = client.getTickCount();
			int miningLevel = client.getBoostedSkillLevel(Skill.MINING);
			int pickaxeId = -1;
			if (pickaxe == null)
			{
				Optional<Pickaxe> pickOp = Pickaxe.getPickaxeFromPlayer(client);
				if (pickOp.isPresent())
				{
					pickaxeId = pickOp.get().itemId;
				}
			}
			else
			{
				pickaxeId = pickaxe.itemId;
			}
			MiningData data = new MiningData(
					miningLevel,
					startTick,
					endTick,
					oreTicks,
					gemTicks,
					ranToRock,
					pickaxeId,
					amulet,
					rockId,
					rockLocation,
					reason
			);
			manager.storeEvent(data);

			if (!oreTicks.isEmpty() && oreTicks.get(oreTicks.size() - 1) == endTick)
			{
				coolDown = 0;
			}
			else
			{
				coolDown = pickaxe == null ? 8 : pickaxe.maxTicks;
			}

			oreTicks = new ArrayList<>();
			gemTicks = new ArrayList<>();
		}
		pickaxe = null;
		state = SkillingState.RECOVERING;
		lastExperimentEnd = client.getTickCount();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		final String message = event.getMessage();
		final ChatMessageType type = event.getType();
		if (state == SkillingState.CLICKED && type == ChatMessageType.SPAM && message.equals(SWING_MESSAGE))
		{
			startTick = client.getTickCount();
			state = SkillingState.MINING;
			amulet = Optional.ofNullable(client.getItemContainer(InventoryID.EQUIPMENT))
					.map(c -> c.getItem(EquipmentInventorySlot.AMULET.getSlotIdx()))
					.map(Item::getId)
					.orElse(-1);
			ranToRock = !client.getLocalPlayer().getWorldLocation().equals(startPoint);
		}
		else if (state == SkillingState.MINING && type == ChatMessageType.SPAM && rockType.isSuccessMsg(message))
		{
			oreTicks.add(client.getTickCount());
		}
		else if (state == SkillingState.MINING && type == ChatMessageType.GAMEMESSAGE && message.equals(INVENTORY_FULL_MESSAGE))
		{
			endExperiment(SkillingEndReason.INVENTORY_FULL);
		}
		else if (state == SkillingState.MINING && type == ChatMessageType.SPAM && message.startsWith(GEM_MESSAGE))
		{
			gemTicks.add(client.getTickCount());
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		int animId = client.getLocalPlayer().getAnimation();
		if (state == SkillingState.MINING)
		{
			Optional<Pickaxe> pickOp = Pickaxe.getPickaxeFromAnim(animId);
			if (pickOp.isPresent())
			{
				pickaxe = pickOp.get();
			}
			else
			{
				endExperiment(SkillingEndReason.INTERRUPTED);
			}
		}
		else if (animId != -1)
		{
			endExperiment(SkillingEndReason.INTERRUPTED);
		}
		else if (state == SkillingState.RECOVERING && client.getTickCount() - lastExperimentEnd >= coolDown)
		{
			state = SkillingState.READY;
		}
		else if (state == SkillingState.CLICKED && client.getTickCount() - lastExperimentEnd >= 20)
		{
			state = SkillingState.READY;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		if (widgetLoaded.getGroupId() == 229)
		{
			Widget widget = client.getWidget(widgetLoaded.getGroupId(), 0);
			if (widget == null)
			{
				return;
			}
			if (widget.getText().startsWith(INVENTORY_FULL_MESSAGE))
			{
				endExperiment(SkillingEndReason.INVENTORY_FULL);
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		MenuAction menuAction = menuOptionClicked.getMenuAction();
		int id = menuOptionClicked.getId();
		if (state == SkillingState.READY && menuAction == MenuAction.GAME_OBJECT_FIRST_OPTION && ORE_OBJECTS.containsKey(id))
		{
			state = SkillingState.CLICKED;
			lastExperimentEnd = client.getTickCount();
			rockId = id;
			rockType = ORE_OBJECTS.get(id);
			rockLocation = WorldPoint.fromScene(client, menuOptionClicked.getActionParam(), menuOptionClicked.getWidgetId(), client.getPlane());
			startPoint = client.getLocalPlayer().getWorldLocation();
		}
		else
		{
			endExperiment(SkillingEndReason.INTERRUPTED);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (state != SkillingState.MINING)
		{
			return;
		}
		if (rockId == event.getGameObject().getId() && rockLocation.equals(event.getTile().getWorldLocation()))
		{
			endExperiment(SkillingEndReason.DEPLETED);
		}
	}
}
