package com.Crowdsourcing.hunter;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class HunterData
{
    private final int level;
    private final String trapName;
    private final int trapId;
    private final WorldPoint trapLocation;
}
