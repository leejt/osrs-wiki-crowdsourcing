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

import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Optional;

public enum Pickaxe
{
	CRYSTAL(71, ItemID.CRYSTAL_PICKAXE, 3, AnimationID.MINING_CRYSTAL_PICKAXE, AnimationID.MINING_MOTHERLODE_CRYSTAL),
	INFERNAL(61, ItemID.INFERNAL_PICKAXE, 3, AnimationID.MINING_INFERNAL_PICKAXE, AnimationID.MINING_MOTHERLODE_INFERNAL),
	DRAGON(61, ItemID.DRAGON_PICKAXE, 3, AnimationID.MINING_DRAGON_PICKAXE, AnimationID.MINING_MOTHERLODE_DRAGON),
	THIRD_AGE(61, ItemID._3RD_AGE_PICKAXE, 3, AnimationID.MINING_3A_PICKAXE, AnimationID.MINING_MOTHERLODE_3A),
	DRAGON_OR(61, ItemID.DRAGON_PICKAXE_OR, 3, AnimationID.MINING_DRAGON_PICKAXE_OR, AnimationID.MINING_MOTHERLODE_DRAGON_OR),
	DRAGON_UPGRADED(61, ItemID.DRAGON_PICKAXE_12797, 3, AnimationID.MINING_DRAGON_PICKAXE_UPGRADED, AnimationID.MINING_MOTHERLODE_DRAGON_UPGRADED),
	DRAGON_OR_TRAILBLAZER(61, ItemID.DRAGON_PICKAXE_OR_25376, 3, AnimationID.MINING_DRAGON_PICKAXE_OR_TRAILBLAZER, AnimationID.MINING_MOTHERLODE_DRAGON_OR_TRAILBLAZER),
	CRYSTAL_INACTIVE(71, ItemID.CRYSTAL_PICKAXE_INACTIVE, 3, AnimationID.MINING_CRYSTAL_PICKAXE, AnimationID.MINING_MOTHERLODE_CRYSTAL),
	INFERNAL_UNCHARGED(61, ItemID.INFERNAL_AXE_UNCHARGED, 3, AnimationID.MINING_INFERNAL_PICKAXE, AnimationID.MINING_MOTHERLODE_INFERNAL),
	GILDED(41, ItemID.GILDED_PICKAXE, 3, AnimationID.MINING_GILDED_PICKAXE, AnimationID.MINING_MOTHERLODE_GILDED),
	RUNE(41, ItemID.RUNE_PICKAXE, 3, AnimationID.MINING_RUNE_PICKAXE, AnimationID.MINING_MOTHERLODE_RUNE),
	ADAMANT(31, ItemID.ADAMANT_PICKAXE, 4, AnimationID.MINING_ADAMANT_PICKAXE, AnimationID.MINING_MOTHERLODE_ADAMANT),
	MITHRIL(21, ItemID.MITHRIL_PICKAXE, 5, AnimationID.MINING_MITHRIL_PICKAXE, AnimationID.MINING_MOTHERLODE_MITHRIL),
	BLACK(11, ItemID.BLACK_PICKAXE, 5, AnimationID.MINING_BLACK_PICKAXE, AnimationID.MINING_MOTHERLODE_BLACK),
	STEEL(6, ItemID.STEEL_PICKAXE, 6, AnimationID.MINING_STEEL_PICKAXE, AnimationID.MINING_MOTHERLODE_STEEL),
	IRON(1, ItemID.IRON_PICKAXE, 7, AnimationID.MINING_IRON_PICKAXE, AnimationID.MINING_MOTHERLODE_IRON),
	BRONZE(1, ItemID.BRONZE_PICKAXE, 8, AnimationID.MINING_BRONZE_PICKAXE, AnimationID.MINING_MOTHERLODE_BRONZE),
	;

	private static final Pickaxe[] values = Pickaxe.values();

	final int level;
	final int itemId;
	final int maxTicks;
	final int[] animIds;

	Pickaxe(int level, int itemId, int maxTicks, int... animIds)
	{
		this.level = level;
		this.itemId = itemId;
		this.maxTicks = maxTicks;
		this.animIds = animIds;
	}

	boolean meetsReqs(Client client)
	{
		return client.getRealSkillLevel(Skill.MINING) >= level;
	}

	static Optional<Pickaxe> getPickaxeFromAnim(int animId)
	{
		for (Pickaxe pickaxe : values)
		{
			if (ArrayUtils.contains(pickaxe.animIds, animId))
			{
				return Optional.of(pickaxe);
			}
		}
		return Optional.empty();
	}

	static Optional<Pickaxe> getPickaxeFromPlayer(Client client)
	{
		int weapon = Optional.ofNullable(client.getItemContainer(InventoryID.EQUIPMENT))
				.map(c -> c.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx()))
				.map(Item::getId)
				.orElse(-1);
		for (Pickaxe pickaxe : values)
		{
			if (!pickaxe.meetsReqs(client))
			{
				continue;
			}
			if (weapon == pickaxe.itemId || client.getItemContainer(InventoryID.INVENTORY).contains(pickaxe.itemId))
			{
				return Optional.of(pickaxe);
			}
		}
		return Optional.empty();
	}
}
