/*
 * Copyright (c) 2021, ThePharros and leejt
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

package com.Crowdsourcing.mlm;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.EvictingQueue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.NullObjectID;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import com.Crowdsourcing.CrowdsourcingManager;

@Slf4j
public class CrowdsourcingMLM
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	private static final String CHAT_MESSAGE_PAYDIRT = "You manage to mine some pay-dirt.";
	private static final String COLLECT_ORE_FROM_SACK = "You collect your ore from the sack.";
	private static final int MOTHERLODE_MINE_REGION_ID = 14936;
	// This is the multiloc; the transformed ID is 26678.
	private static final int SACK_ID = NullObjectID.NULL_26688;

	private Multiset<Integer> prevInventorySnapshot;

	// Maximum remainder to track: size of sack, plus size of inventory
	private final EvictingQueue<PaydirtMineData> paydirtMineData = EvictingQueue.create(189 + 28);
	private boolean waitingForOre = false;

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getLocalPlayer().getWorldLocation().getRegionID() != MOTHERLODE_MINE_REGION_ID)
		{
			return;
		}

		if (!waitingForOre) {
			return;
		}

		Widget widgetSpriteText = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);

		if (widgetSpriteText == null)
		{
			return;
		}

		if (widgetSpriteText.getText().startsWith(COLLECT_ORE_FROM_SACK)) {
			waitingForOre = false;
			final Multiset<Integer> currentInventorySnapshot = getInventorySnapshot();
			final Multiset<Integer> rewards = Multisets.difference(currentInventorySnapshot, prevInventorySnapshot);
			final int currentSackCount = client.getVarbitValue(Varbits.SACK_NUMBER);
			MLMData event = new MLMData(
				new ArrayList(paydirtMineData),
				rewards,
				currentSackCount,
				currentInventorySnapshot.count(ItemID.PAYDIRT)
			);
			manager.storeEvent(event);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		MenuAction menuAction = menuOptionClicked.getMenuAction();
		if (menuOptionClicked.getId() == SACK_ID && menuAction == MenuAction.GAME_OBJECT_FIRST_OPTION) {
			prevInventorySnapshot = getInventorySnapshot();
			waitingForOre = true;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		if (event.getMessage().equals(CHAT_MESSAGE_PAYDIRT))
		{
			paydirtMineData.add(new PaydirtMineData(
				client.getRealSkillLevel(Skill.MINING),
				client.getBoostedSkillLevel(Skill.MINING),
				getRingId(),
				getDiaryCompletions(),
				Instant.now().getEpochSecond()
			));
		}
	}

	private Multiset<Integer> getInventorySnapshot()
	{
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		Multiset<Integer> inventorySnapshot = HashMultiset.create();

		if (inventory != null)
		{
			Arrays.stream(inventory.getItems())
				.forEach(item -> inventorySnapshot.add(item.getId(), item.getQuantity()));
		}

		return inventorySnapshot;
	}

	private int getRingId()
	{
		ItemContainer equipContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipContainer != null)
		{
			final Item[] items = equipContainer.getItems();
			int idx = EquipmentInventorySlot.RING.getSlotIdx();
			if (idx < items.length)
			{
				return equipContainer.getItems()[idx].getId();
			}
		}
		return -1;
	}

	private int getDiaryCompletions()
	{
		int easy = client.getVarbitValue(Varbits.DIARY_FALADOR_EASY);
		int medium = client.getVarbitValue(Varbits.DIARY_FALADOR_MEDIUM);
		int hard = client.getVarbitValue(Varbits.DIARY_FALADOR_HARD);
		int elite = client.getVarbitValue(Varbits.DIARY_FALADOR_ELITE);

		return easy + 2*medium + 4*hard + 8*elite;
	}
}
