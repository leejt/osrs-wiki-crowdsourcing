package com.Crowdsourcing.hunter;

import com.Crowdsourcing.CrowdsourcingManager;
import com.Crowdsourcing.skilling.SkillingState;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

public class CrowdsourcingHunter {
    @Inject
    private CrowdsourcingManager manager;

    @Inject
    private Client client;

    private static final String FALCONRY_SUCCESS = "The falcon successfully swoops down and captures the kebbit.";
    private static final String FALCONRY_FAILURE = "The falcon swoops down on the kebbit, but just misses catching it.";

    private static final Map<Integer, String> TRAP_OBJECTS = new ImmutableMap.Builder<Integer, String>().
        put(721, "Black chinchompa success").
        put(9382, "Grey chinchompa success").
        put(9383, "Red chinchompa success").
        put(9384, "Ferret box trap").
        put(ObjectID.BOX_TRAP_9385, "Failed box trap").
        put(ObjectID.BIRD_SNARE_9379, "Copper longtail success").
        put(ObjectID.BIRD_SNARE_9377, "Golden warbler success").
        put(ObjectID.BIRD_SNARE_9375, "Cerulean twitch success").
        put(ObjectID.BIRD_SNARE_9373, "Crimson switch success").
        put(ObjectID.BIRD_SNARE_9348, "Tropical wagtail success").
        put(9344, "Failed bird snare").
//        put(19253, "Pit (Snow)").
//        put(19254, "Pit (Snow)").
//        put(19255, "Pit (Snow)").
//        put(19256, "Pit (Snow)").
//        put(19257, "Pit (Snow)").
//        put(19258, "Pit (Snow)").
//        put(19259, "Pit (Jungle)").
//        put(19260, "Pit (Jungle)").
//        put(19261, "Pit (Jungle)").
//        put(19262, "Pit (Jungle)").
//        put(19263, "Pit (Jungle)").
//        put(19264, "Pit (Karamja)").
//        put(19265, "Pit (Karamja)").
//        put(19266, "Pit (Karamja)").
//        put(19267, "Pit (Karamja)").
//        put(19268, "Pit (Karamja)").
//        put(ObjectID.NET_TRAP_8996, "Net trap"). // Success
//        put(ObjectID.NET_TRAP_8998, "Net trap"). // Failed
        build();

    private static final Map<Integer, String> ANIMALS = new ImmutableMap.Builder<Integer, String>().
        put(5531, "Spotted kebbit").
        put(5532, "Dark kebbit").
        put(5533, "Dashing kebbit").
        build();


    private static final Set<Integer> ANIMATIONS = new ImmutableSet.Builder<Integer>().
        add(5212). // Dismantling a box trap
        add(5207). // Dismantling a net trap and bird snare
//        add(6606). // Butterfly net catching
        build();

    private int hunterLevel;
    private int id;
    private String name;
    private WorldPoint location;
    private SkillingState state = SkillingState.READY;

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        MenuAction menuAction = event.getMenuAction();
        int objectId = event.getId();
        // Check for typical trap scenario (box and bird snare)
        if (menuAction == MenuAction.GAME_OBJECT_FIRST_OPTION && TRAP_OBJECTS.containsKey(objectId))
        {
            state = SkillingState.DISMANTLING;
            name = TRAP_OBJECTS.get(objectId);
            id = objectId;
            hunterLevel = client.getBoostedSkillLevel(Skill.HUNTER);
            location = WorldPoint.fromScene(client, event.getActionParam(), event.getWidgetId(), client.getPlane());
        }
        // Check for falconry event
        else if (event.getMenuOption().equals("Catch") && ANIMALS.containsKey(id))
        {
            state = SkillingState.FALCON_PUNCHING;
            name = ANIMALS.get(id);
            hunterLevel = client.getBoostedSkillLevel(Skill.HUNTER);
            location = WorldPoint.fromScene(client, event.getActionParam(), event.getWidgetId(), client.getPlane());
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        int animId = client.getLocalPlayer().getAnimation();
        if (ANIMATIONS.contains(animId))
        {
            if (state == SkillingState.DISMANTLING)
            {
                state = SkillingState.READY;
                HunterData data = new HunterData(hunterLevel, name, id, null, location);
                manager.storeEvent(data);
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.SPAM)
        {
            return;
        }

        String message = event.getMessage();
        if (FALCONRY_SUCCESS.equals(message) || FALCONRY_FAILURE.equals(message))
        {
            state = SkillingState.READY;
            HunterData data = new HunterData(hunterLevel, name, id, message, location);
            manager.storeEvent(data);
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
    {
        // Check for active falconry send out
        if (menuEntryAdded.getType() == MenuAction.NPC_FIRST_OPTION.getId() && state != SkillingState.FALCON_PUNCHING)
        {
            int npcIndex = menuEntryAdded.getIdentifier();
            NPC npc = client.getCachedNPCs()[npcIndex];

            if (npc == null)
            {
                return;
            }

            id = npc.getId();
        }
    }
}
