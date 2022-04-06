package com.Crowdsourcing.npc_respawn;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class NpcRespawnData
{
    private int npcIndex;
    private int npcId;
    private int respawnTime;
    private WorldPoint location;
    private boolean isInInstance;
}
