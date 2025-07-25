package com.Crowdsourcing.impling;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;

import com.google.common.collect.ImmutableSet;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.Subscribe;

import java.util.Set;

public class CrowdsourcingImpling {

    private static final int MAX_ACTOR_VIEW_RANGE = 15;
    private WorldPoint lastPlayerLocation;

	private static final Set<Integer> INVISIBLE_IDS = ImmutableSet.of(
		NpcID.II_RARE_IMPLING_PRECURSOR,
		NpcID.II_RARE_IMPLING_PRECURSOR_MAZE,
		NpcID.II_UNCOMMON_IMPLING_PRECURSOR,
		NpcID.II_COMMON_IMPLING_PRECURSOR,
		NpcID.II_IMPLING_TYPE_12_PRECURSOR,
		NpcID.II_MAZE_BLOCKING_TIMER,
		NpcID.II_MAZE_GATE_TIMER,
		NpcID.II_IMPLING_ADDER,
		NpcID.II_CROP_CIRCLE_NPC);

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
        int id = npc.getComposition().getId();
        if (!INVISIBLE_IDS.contains(id)) {
            return;
        }

        WorldPoint npcLocation = npc.getWorldLocation();
        if (lastPlayerLocation != null && isInViewRange(lastPlayerLocation, npcLocation)) {
            ImplingData data = new ImplingData(id, -1, npcLocation);
            manager.storeEvent(data);
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged npcChanged)
    {
        final NPC npc = npcChanged.getNpc();
        final int id = npc.getComposition().getId();
        final int oldId = npcChanged.getOld().getId();
        if (!INVISIBLE_IDS.contains(id) && !INVISIBLE_IDS.contains(oldId)) {
            return;
        }

        WorldPoint npcLocation = npc.getWorldLocation();
        ImplingData data = new ImplingData(id, oldId, npcLocation);
        manager.storeEvent(data);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGIN_SCREEN ||
                event.getGameState() == GameState.HOPPING)
        {
            lastPlayerLocation = null;
        }
    }
}
