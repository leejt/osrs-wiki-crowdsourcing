package com.Crowdsourcing.clues;


import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;

import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import com.Crowdsourcing.CrowdsourcingManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static net.runelite.api.MenuAction.ITEM_FIRST_OPTION;

@Slf4j
public class CrowdsourcingClues {
    @Inject
    private Client client;

    @Inject
    private CrowdsourcingManager manager;

    @Inject
    private ItemManager itemManager;

    private HashSet<Integer> seenClues = new HashSet<>();
    private Widget mapClueWidgetParent;
    private Integer clueId = -1;
    private String clueText;
    private List<MapClueWidgetPart> parts;

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        if ("Read".equals(event.getMenuOption()) && event.getMenuAction() == ITEM_FIRST_OPTION) {
            final ItemComposition itemComposition = itemManager.getItemComposition(event.getId());

            if (itemComposition.getName().startsWith("Clue scroll") || itemComposition.getName().startsWith("Challenge scroll")) {
                if (!seenClues.contains(itemComposition.getId())) {
                    clueId = itemComposition.getId();
                    seenClues.add(clueId);
                }
            }
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        int groupId = event.getGroupId();
        for (int childId = 0; childId < 100; childId++) {
            final Widget w = client.getWidget(groupId, childId);
            if (w == null) {
                break;
            }
            // These are the corners of the map clue. We couldn't figure out
            // a better way to do this.
            if (w.getModelId() == 3388) {
                mapClueWidgetParent = w.getParent();
            }
        }
    }
    @Subscribe
    public void onGameTick(GameTick event) {
       if (clueId == -1) {
           return;
       }
       final Widget clueTextWidget = client.getWidget(WidgetInfo.CLUE_SCROLL_TEXT);
       if (clueTextWidget != null) {
           clueText = clueTextWidget.getText();
           submitClue();
           return;
       }

        if (mapClueWidgetParent != null) {
            parts = new ArrayList<>();
            for (Widget child : mapClueWidgetParent.getNestedChildren()) {
                parts.add(new MapClueWidgetPart(
                        child.getRelativeX(),
                        child.getRelativeY(),
                        child.getRotationX(),
                        child.getRotationY(),
                        child.getRotationZ(),
                        child.getModelId(),
                        child.getModelZoom()
                ));
            }
            submitClue();
        }
    }

    private void submitClue() {
        manager.storeEvent(new ClueData(clueId, clueText, parts));
        log.info("{}, {}, {}", clueId, clueText, parts);
        clueId = -1;
        clueText = "";
        parts = null;
        mapClueWidgetParent = null;

    }
}