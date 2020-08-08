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
        put(9383, "Box trap"). // Successful box trap
        put(ObjectID.BOX_TRAP_9385, "Box trap"). // Failed box trap
        build();

    private static final Set<Integer> ANIMATIONS = new ImmutableSet.Builder<Integer>().
        add(5212). // Dismantling a trap
        add(5207). // Dismantling a net trap
        build();

    private int hunterLevel;
    private int trapId;
    private String trapName;
    private WorldPoint trapLocation;
    private SkillingState state = SkillingState.READY;

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
    {
        MenuAction menuAction = menuOptionClicked.getMenuAction();
        int id = menuOptionClicked.getId();
        if (menuAction == MenuAction.GAME_OBJECT_FIRST_OPTION && TRAP_OBJECTS.containsKey(id))
        {
            state = SkillingState.DISMANTLING;
            trapName = TRAP_OBJECTS.get(id);
            trapId = id;
            hunterLevel = client.getBoostedSkillLevel(Skill.HUNTER);
            trapLocation = WorldPoint.fromScene(client, menuOptionClicked.getActionParam(), menuOptionClicked.getWidgetId(), client.getPlane());
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        int animId = client.getLocalPlayer().getAnimation();
        if (ANIMATIONS.contains(animId) && state == SkillingState.DISMANTLING)
        {
            state = SkillingState.READY;
            HunterData data = new HunterData(hunterLevel, trapName, trapId, trapLocation);
            manager.storeEvent(data);
        }
    }
}
