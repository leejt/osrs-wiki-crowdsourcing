package com.Crowdsourcing.monster_examine;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
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
			lastId = client.getTopLevelWorldView().npcs().byIndex(event.getId()).getComposition().getId();
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
            String name = client.getWidget(InterfaceID.DreamMonsterStat.MONSTER_NAME).getText();
            String stats = client.getWidget(InterfaceID.DreamMonsterStat.MONSTER_STATS).getText();
            String aggressive = client.getWidget(InterfaceID.DreamMonsterStat.MONSTER_AGGRESSIVE).getText();
            String defensive = client.getWidget(InterfaceID.DreamMonsterStat.MONSTER_DEFENSIVE).getText();
            String other = client.getWidget(InterfaceID.DreamMonsterStat.MONSTER_OTHER).getText();
            httpClient.submitToAPI(
                    new MonsterExamineData(lastId, name, stats ,aggressive, defensive, other, client.getLocalPlayer().getName()));
        }
    }


}
