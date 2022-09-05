package com.Crowdsourcing.impling;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class ImplingData
{
    private int npcId;
    private int oldNpcId;
    private WorldPoint location;
}
