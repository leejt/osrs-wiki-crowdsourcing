package com.Crowdsourcing.monster_examine;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class MonsterExamine
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MonsterExamineOverlay overlay;

    @Inject
    private MonsterExamineClient httpClient;

    int lastId = -1;
    Set<Integer> seenIds = new HashSet<>();

    public void startUp()
    {
        overlayManager.add(overlay);
        httpClient.getSeenIds();
    }

    public void shutDown()
    {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        String target = event.getMenuTarget();
        if (target.startsWith("<col=00ff00>Monster Examine</col><col=ffffff> -> "))
        {
            lastId = client.getCachedNPCs()[event.getId()].getComposition().getId();
        }
    }

    public void setSeenIds(Set<Integer> seenIds)
    {
        this.seenIds = seenIds;
    }

    public Set<Integer> getSeenIds()
    {
        return this.seenIds;
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event)
    {
        if (event.getScriptId() == 1179)
        {
            String name = client.getWidget(522, 3).getText();
            String stats = client.getWidget(522, 20).getText();
            String aggressive = client.getWidget(522, 22).getText();
            String defensive = client.getWidget(522, 24).getText();
            String other = client.getWidget(522, 26).getText();
            httpClient.submitToAPI(
                    new MonsterExamineData(lastId, name, stats ,aggressive, defensive, other));
        }
    }


}
