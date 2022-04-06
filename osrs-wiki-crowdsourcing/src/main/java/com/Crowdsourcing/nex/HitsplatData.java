package com.Crowdsourcing.nex;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Hitsplat;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class HitsplatData
{
    private int npcId;
    private Hitsplat.HitsplatType type;
    private int amount;
    private int healthRatio;
    private int healthScale;
}
