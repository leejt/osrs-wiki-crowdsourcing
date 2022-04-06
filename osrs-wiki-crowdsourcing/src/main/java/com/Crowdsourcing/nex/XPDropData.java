package com.Crowdsourcing.nex;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class XPDropData
{
    private Skill skill;
    private int xpDrop;
    private int npcId;
    private int healthRatio;
    private int healthScale;
}
