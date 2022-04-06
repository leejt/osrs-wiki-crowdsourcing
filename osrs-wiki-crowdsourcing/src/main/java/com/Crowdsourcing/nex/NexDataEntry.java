package com.Crowdsourcing.nex;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class NexDataEntry
{
    private int tickCount;
    private Object data;
}
