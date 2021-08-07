package com.Crowdsourcing.respawns;

import javax.inject.Inject;

import com.Crowdsourcing.CrowdsourcingManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "Respawns",
        description = "Logs scenery and item respawn times to your chatbox."
)
public class Respawns
{
    @Inject
    private Client client;

    @Inject
    private WorldService worldService;

    @Inject
    private CrowdsourcingManager manager;

    @Inject
    private ItemManager itemManager;

    private GameState gameState;

    private static final int MAX_DESPAWN_ENTRIES = 5000;
    private Map<RespawnKey, Integer> despawnedScenery = new LinkedHashMap<RespawnKey, Integer>()
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<RespawnKey, Integer> eldest)
        {
            return size() > MAX_DESPAWN_ENTRIES;
        }
    };

    private Map<RespawnKey, ItemRespawnValue> despawnedItems = new LinkedHashMap<RespawnKey, ItemRespawnValue>()
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<RespawnKey, ItemRespawnValue> eldest)
        {
            return size() > MAX_DESPAWN_ENTRIES;
        }
    };

    @Subscribe
    public void onItemSpawned(ItemSpawned event)
    {
        RespawnKey key = new RespawnKey(event.getTile().getWorldLocation(), event.getItem().getId());
        if (despawnedItems.containsKey(key))
        {
            int diff = client.getTickCount() - despawnedItems.get(key).getTickCount();
            if (diff == 0)
            {
                return;
            }
            int worldPopulation = despawnedItems.get(key).getWorldPopulation();
            int estimate = diff * 4000 / (4000 - worldPopulation) + 1;
            despawnedItems.remove(key);
            String name = itemManager.getItemComposition(key.getId()).getName();

            String message = String.format("ITEM %s: %d ticks on %d population, estimate: %d",
                    name, diff, worldPopulation, estimate + 1);
            manager.sendMessage(message);
        }
    }

    @Subscribe
    public void onItemDespawned(ItemDespawned event)
    {
        int worldPopulation = worldService.getWorlds().findWorld(client.getWorld()).getPlayers();
        RespawnKey key = new RespawnKey(event.getTile().getWorldLocation(), event.getItem().getId());
        despawnedItems.put(key, new ItemRespawnValue(client.getTickCount(), worldPopulation));
    }

    private void scenerySpawn(WorldPoint baseLocation, int id)
    {
        if (gameState != GameState.LOGGED_IN)
        {
            return;
        }
        RespawnKey key = new RespawnKey(baseLocation, id);
        if (despawnedScenery.containsKey(key))
        {
            int diff = client.getTickCount() - despawnedScenery.get(key);
            despawnedScenery.remove(key);
            ObjectComposition objectComposition = client.getObjectDefinition(id);
            objectComposition = objectComposition.getImpostorIds() == null ? objectComposition : objectComposition.getImpostor();
            String name = objectComposition.getName();
            String message = String.format("SCENERY %s (id: %d): %d ticks", name, id, diff + 1);
            manager.sendMessage(message);
        }
    }

    private void sceneryDespawn(WorldPoint baseLocation, int id)
    {
        if (gameState != GameState.LOGGED_IN)
        {
            return;
        }
        RespawnKey key = new RespawnKey(baseLocation, id);
        despawnedScenery.put(key, client.getTickCount());
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
    {
        scenerySpawn(event.getTile().getWorldLocation(), event.getDecorativeObject().getId());
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event)
    {
        scenerySpawn(event.getTile().getWorldLocation(), event.getWallObject().getId());
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        scenerySpawn(event.getTile().getWorldLocation(), event.getGameObject().getId());
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event)
    {
        scenerySpawn(event.getTile().getWorldLocation(), event.getGroundObject().getId());
    }

    @Subscribe
    public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
    {
        sceneryDespawn(event.getTile().getWorldLocation(), event.getDecorativeObject().getId());
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event)
    {
        sceneryDespawn(event.getTile().getWorldLocation(), event.getWallObject().getId());
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        sceneryDespawn(event.getTile().getWorldLocation(), event.getGameObject().getId());
    }

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned event)
    {
        sceneryDespawn(event.getTile().getWorldLocation(), event.getGroundObject().getId());
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        gameState = event.getGameState();
        if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.HOPPING) {
            despawnedScenery.clear();
            despawnedItems.clear();
        }
    }

    @Data
    @AllArgsConstructor
    static class RespawnKey
    {
        private final WorldPoint location;
        private final int id;
    }

    @Data
    @AllArgsConstructor
    static class ItemRespawnValue
    {
        private final int tickCount;
        private final int worldPopulation;
    }
}
