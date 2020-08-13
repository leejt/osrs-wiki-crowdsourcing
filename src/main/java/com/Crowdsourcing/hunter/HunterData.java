package com.Crowdsourcing.hunter;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class HunterData
{
    private final int level;
    private final String name;
    private final int id;
    private final String message;
    private final WorldPoint location;
}
