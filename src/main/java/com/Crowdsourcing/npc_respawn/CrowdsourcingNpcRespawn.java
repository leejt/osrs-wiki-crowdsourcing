package com.Crowdsourcing.npc_respawn;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.Subscribe;

import java.util.HashMap;
import java.util.HashSet;

public class CrowdsourcingNpcRespawn {

    private static final int MAX_ACTOR_VIEW_RANGE = 15;
    private WorldPoint lastPlayerLocation;
    private HashMap<Integer, Integer> npcDespawnTimes = new HashMap<>();
    private HashSet<Integer> seenNpcs = new HashSet<>();

    @Inject
    private Client client;

    @Inject
    public CrowdsourcingManager manager;

    private static boolean isInViewRange(WorldPoint wp1, WorldPoint wp2)
    {
        int distance = wp1.distanceTo(wp2);
        return distance < MAX_ACTOR_VIEW_RANGE;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        lastPlayerLocation = client.getLocalPlayer().getWorldLocation();
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned)
    {
        final NPC npc = npcSpawned.getNpc();
        int index = npc.getIndex();
        if (!npcDespawnTimes.containsKey(index)) {
            return;
        }

        if (lastPlayerLocation != null && isInViewRange(lastPlayerLocation, npc.getWorldLocation())) {
            int respawnTime = client.getTickCount() - npcDespawnTimes.get(index);
            LocalPoint local = LocalPoint.fromWorld(client, npc.getWorldLocation());
            WorldPoint location = null;
            boolean isInInstance = false;
            if (local != null)
            {
                location = WorldPoint.fromLocalInstance(client, local);
                isInInstance = client.isInInstancedRegion();
            }
            manager.storeEvent(new NpcRespawnData(index, npc.getId(), respawnTime, location, isInInstance));
            seenNpcs.add(index);
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned)
    {
        final NPC npc = npcDespawned.getNpc();
        int index = npc.getIndex();
        if (seenNpcs.contains(index)) {
            return;
        }

        if (isInViewRange(client.getLocalPlayer().getWorldLocation(), npc.getWorldLocation())) {
            npcDespawnTimes.put(index, client.getTickCount());
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGIN_SCREEN ||
                event.getGameState() == GameState.HOPPING)
        {
            npcDespawnTimes.clear();
        }
    }
}
