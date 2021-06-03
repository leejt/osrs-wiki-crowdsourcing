/*
 * Copyright (c) 2021, ThePharros
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import java.util.Arrays;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.crowdsourcing.CrowdsourcingManager;

@Slf4j
public class CrowdsourcingMLM
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	//these would normally be part of RL's main branch if the original PR was to be merged
	//instead, we have them exist as inner values for sake of hub usage
	private static final int MOTHERLODE_MINE_SACK_GROUP_ID = 229;
	private static final int MOTHERLODE_MINE_SACK_TEXT_CHILD_ID = 1;


	private static final String CHAT_MESSAGE_PAYDIRT = "You manage to mine some pay-dirt."; //pay-dirt mined
	private static final String MLM_SACK = "You collect your ore from the sack."; //ore collected, more remain
	private static final String MLM_SACK_EMPTIED = "The sack"; //minimum literal string for emptied sack messages
	private static final int MOTHERLODE_MINE_REGION_ID = 14936;
	private static final Map<Integer, Integer> PICKAXE_ANIMS = new ImmutableMap.Builder<Integer, Integer>().
		put(AnimationID.MINING_MOTHERLODE_BRONZE, ItemID.BRONZE_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_IRON, ItemID.IRON_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_STEEL, ItemID.STEEL_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_BLACK, ItemID.BLACK_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_MITHRIL, ItemID.MITHRIL_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_ADAMANT, ItemID.ADAMANT_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_RUNE, ItemID.RUNE_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_DRAGON, ItemID.DRAGON_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_DRAGON_OR, ItemID.DRAGON_PICKAXE_OR).
		put(AnimationID.MINING_MOTHERLODE_DRAGON_OR_TRAILBLAZER, ItemID.DRAGON_PICKAXE_OR_25376).
		put(AnimationID.MINING_MOTHERLODE_DRAGON_UPGRADED, ItemID.DRAGON_PICKAXE_12797).
		put(AnimationID.MINING_MOTHERLODE_INFERNAL, ItemID.INFERNAL_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_3A, ItemID._3RD_AGE_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_CRYSTAL, ItemID.CRYSTAL_PICKAXE).
		put(AnimationID.MINING_MOTHERLODE_TRAILBLAZER, ItemID.INFERNAL_PICKAXE_OR).
		put(AnimationID.MINING_MOTHERLODE_GILDED, ItemID.GILDED_PICKAXE).build();

	private int invChangedTick = -1;
	private int miningBoostedLevel = -1;
	private int pickaxe = -1;
	private int ring = -1;
	private int diaries = -1;

	private boolean levelChanged = false;
	private boolean pickaxeChanged = false;
	private boolean ringChanged = false;
	private boolean diariesChanged = false;
	private boolean sackRefreshed = false; //used to ensure the session's data is fresh

	private Multiset<Integer> previousInventorySnapshot;
	private Multiset<Integer> rewards;

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getLocalPlayer().getWorldLocation().getRegionID() != MOTHERLODE_MINE_REGION_ID)
		{
			return;
		}

		setPickaxeData(); //set used pickaxe every tick

		//get MLM sack widgets to know when sack is emptied
		Widget widgetMLM = client.getWidget(MOTHERLODE_MINE_SACK_GROUP_ID, MOTHERLODE_MINE_SACK_TEXT_CHILD_ID);
		Widget widgetMLMSprite = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
		final int tick = client.getTickCount();

		if ((widgetMLM != null && widgetMLM.getText().startsWith(MLM_SACK_EMPTIED)) ||
			(widgetMLMSprite != null && widgetMLMSprite.getText().contains(MLM_SACK_EMPTIED)))
		{
			if (!sackRefreshed)
			{
				sackRefreshed = true; //ensures sack has been emptied and current data session isn't potentially tainted
			} else if (tick == invChangedTick) //submit data when sack is looted
			{
				setRewardsData();
				submitData();
				resetData(); //we reset the session data since sack has been emptied here
				return;
			}
			return;
		}

		if (widgetMLMSprite != null && widgetMLMSprite.getText().contains(MLM_SACK)) //sack is looted but not emptied
		{
			if (sackRefreshed && tick == invChangedTick) //submit data when sack is looted
			{
				setRewardsData();
				submitData();
				return;
			}
			return;
		}

		previousInventorySnapshot = getInventorySnapshot(); //gets inventory before sack is looted
	}

	//get the game tick when inv changes in MLM; used to compare to same tick sack is looted at
	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getItemContainer() != client.getItemContainer(InventoryID.INVENTORY) ||
			client.getLocalPlayer().getWorldLocation().getRegionID() != MOTHERLODE_MINE_REGION_ID)
		{
			return;
		}
		invChangedTick = client.getTickCount();
	}

	//sets mining lvl, ring, and diary data when a pay-dirt is mined, since
	//pay-dirt rolls are determined at the time they are mined, NOT when the sack gets looted
	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SPAM || !sackRefreshed)
		{
			return;
		}

		if (event.getMessage().equals(CHAT_MESSAGE_PAYDIRT)) //happens 1 tick after pickaxe animation
		{
			setMiningLevelData();
			setRingData();
			setDiaryData();
		}
	}

	//gets inventory data by item id and quantity
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

	private void setRewardsData()
	{
		Multiset<Integer> currentInventorySnapshot = getInventorySnapshot();
		final Multiset<Integer> rewardsSnapshot = Multisets.difference(currentInventorySnapshot, previousInventorySnapshot);
		rewards = rewardsSnapshot;
	}

	private void setMiningLevelData()
	{
		int currLevel = client.getBoostedSkillLevel(Skill.MINING);
		if (miningBoostedLevel != -1 && miningBoostedLevel != currLevel)
		{
			levelChanged = true; //lets us know if Mining lvl changed throughout pay-dirt mining session
		}
		miningBoostedLevel = currLevel;
	}

	private void setPickaxeData()
	{
		int animId = client.getLocalPlayer().getAnimation();
		if (PICKAXE_ANIMS.containsKey(animId))
		{
			int currPickaxe = PICKAXE_ANIMS.get(animId);
			if (pickaxe != -1 && pickaxe != currPickaxe)
			{
				pickaxeChanged = true; //lets us know if pickaxe changed throughout pay-dirt mining session
			}
			pickaxe = currPickaxe;
		}
	}

	private void setRingData()
	{
		ItemContainer equipContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipContainer != null)
		{
			int currRingId = equipContainer.getItems()[EquipmentInventorySlot.RING.getSlotIdx()].getId();
			if (ring != -1 && ring != currRingId)
			{
				ringChanged = true; //lets us know if ring changed throughout pay-dirt mining session
			}
			ring = currRingId;
		}
	}

	private void setDiaryData()
	{
		int easy = client.getVar(Varbits.DIARY_FALADOR_EASY);
		int medium = client.getVar(Varbits.DIARY_FALADOR_MEDIUM);
		int hard = client.getVar(Varbits.DIARY_FALADOR_HARD);
		int elite = client.getVar(Varbits.DIARY_FALADOR_ELITE);

		int currDiaries = easy + 2*medium + 4*hard + 8*elite;
		if (diaries != -1 && diaries != currDiaries)
		{
			diariesChanged = true; //lets us know if diaries changed throughout pay-dirt mining session
		}
		diaries = currDiaries;
	}

	private void submitData()
	{
		if (rewards == null)
		{
			return;
		}

		MLMData data = new MLMData(
			rewards,
			miningBoostedLevel,
			pickaxe,
			ring,
			diaries,
			levelChanged,
			pickaxeChanged,
			ringChanged,
			diariesChanged);
		//printData();
		manager.storeEvent(data);
	}

	//resets all data to default/null values
	//this is used to ensure each data submitted session is consistent from start to finish
	private void resetData()
	{
		miningBoostedLevel = -1;
		pickaxe = -1;
		ring = -1;
		diaries = -1;

		levelChanged = false;
		pickaxeChanged = false;
		ringChanged = false;
		diariesChanged = false;

		rewards = null;
	}

	//debug printer
	private void printData()
	{
		log.debug("\n===== MLM DATA =====\nLevel: " + miningBoostedLevel + "\nPickaxe: " +
			pickaxe + "\nRing: " + ring + "\nDiary sum: " + diaries +
			"\n\nLevel changed?: " + levelChanged + "\nPickaxe changed?: " + pickaxeChanged +
			"\nRing changed?: " + ringChanged + "\nDiaries changed?: " + diariesChanged);

		log.debug("===== REWARDS =====");
		for (Integer reward : rewards.elementSet())
		{
			log.debug("REWARD: " + reward + " : " + rewards.count(reward));
		}
	}

}