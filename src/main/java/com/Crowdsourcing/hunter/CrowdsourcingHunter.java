package com.Crowdsourcing.hunter;

import com.Crowdsourcing.CrowdsourcingManager;
import com.Crowdsourcing.skilling.SkillingState;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectID;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
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

    private static final Map<Integer, String> TRAP_OBJECTS = new ImmutableMap.Builder<Integer, String>().
        put(721, "Black chinchompa box trap"). // Black chin successful box trap
        put(9382, "Grey chinchompa box trap"). // Grey chin successful box trap
        put(9383, "Red chinchompa box trap"). // Red chin successful box trap
        put(9384, "Ferret box trap"). // Ferret successful box trap
        put(ObjectID.BOX_TRAP_9385, "Failed box trap"). // Failed box trap
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

    private static final Set<Integer> ANIMATIONS = new ImmutableSet.Builder<Integer>().
        add(5212). // Dismantling a trap
//        add(5207). // Dismantling a net trap and bird snare
//        add(6606). // Butterfly net catching
        build();

    private int hunterLevel;
    private int id;
    private String name;
    private WorldPoint location;
    private SkillingState state = SkillingState.READY;

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
    {
        MenuAction menuAction = menuOptionClicked.getMenuAction();
        int objectId = menuOptionClicked.getId();
        if (menuAction == MenuAction.GAME_OBJECT_FIRST_OPTION && TRAP_OBJECTS.containsKey(id))
        {
            state = SkillingState.DISMANTLING;
            name = TRAP_OBJECTS.get(objectId);
            id = objectId;
            hunterLevel = client.getBoostedSkillLevel(Skill.HUNTER);
            location = WorldPoint.fromScene(client, menuOptionClicked.getActionParam(), menuOptionClicked.getWidgetId(), client.getPlane());
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
                HunterData data = new HunterData(hunterLevel, name, id, location);
                manager.storeEvent(data);
            }
        }
    }
}
