package com.Crowdsourcing.toa;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.Getter;
import static net.runelite.api.ItemID.ADAMANT_PICKAXE;
import static net.runelite.api.ItemID.BLACK_PICKAXE;
import static net.runelite.api.ItemID.BRONZE_PICKAXE;
import static net.runelite.api.ItemID.CRYSTAL_PICKAXE;
import static net.runelite.api.ItemID.DRAGON_PICKAXE;
import static net.runelite.api.ItemID.DRAGON_PICKAXE_OR;
import static net.runelite.api.ItemID.DRAGON_PICKAXE_OR_25376;
import static net.runelite.api.ItemID.DRAGON_PICKAXE_12797;
import static net.runelite.api.ItemID.GILDED_PICKAXE;
import static net.runelite.api.ItemID.INFERNAL_PICKAXE;
import static net.runelite.api.ItemID.IRON_PICKAXE;
import static net.runelite.api.ItemID.MITHRIL_PICKAXE;
import static net.runelite.api.ItemID.RUNE_PICKAXE;
import static net.runelite.api.ItemID.STEEL_PICKAXE;
import static net.runelite.api.ItemID._3RD_AGE_PICKAXE;
import static net.runelite.api.ItemID.INFERNAL_PICKAXE_OR;

@Getter
enum Pickaxe
{
	//TODO Not all pickaxes are mapped for mining Het's Seal, additional mapping required
	BRONZE(BRONZE_PICKAXE, 9473),
	IRON(IRON_PICKAXE, 9474),
	STEEL(STEEL_PICKAXE, 9476),
	BLACK(BLACK_PICKAXE, 9475),
	MITHRIL(MITHRIL_PICKAXE, 9477),
	ADAMANT(ADAMANT_PICKAXE, 9478),
	RUNE(RUNE_PICKAXE, 9479),
	//GILDED(GILDED_PICKAXE, ),
	DRAGON(DRAGON_PICKAXE, 9481);
	//DRAGON_OR(DRAGON_PICKAXE_OR, ),
	//DRAGON_OR_TRAILBLAZER(DRAGON_PICKAXE_OR_25376, ),
	//DRAGON_UPGRADED(DRAGON_PICKAXE_12797, ),
	//INFERNAL(INFERNAL_PICKAXE, ),
	//THIRDAGE(_3RD_AGE_PICKAXE, ),
	//CRYSTAL(CRYSTAL_PICKAXE, ),
	//TRAILBLAZER(INFERNAL_PICKAXE_OR, );

	private final int itemId;
	private final int[] animIds;

	private static final Map<Integer, Pickaxe> PICKAXE_ANIM_IDS;

	static
	{
		ImmutableMap.Builder<Integer, Pickaxe> builder = new ImmutableMap.Builder<>();

		for (Pickaxe pickaxe : values())
		{
			for (int animId : pickaxe.animIds)
			{
				builder.put(animId, pickaxe);
			}
		}

		PICKAXE_ANIM_IDS = builder.build();
	}

	Pickaxe(int itemId, int ... animIds)
	{
		this.itemId = itemId;
		this.animIds = animIds;
	}

	static Pickaxe fromAnimation(int animId)
	{
		return PICKAXE_ANIM_IDS.get(animId);
	}
}