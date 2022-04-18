package com.Crowdsourcing.mahogany_homes;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
public class CrowdsourcingMahoganyHomes {

    @Inject
    public Client client;

    private static int lastAction = -1;
    private String lastNpcName = null;

    private final WorldPoint AMY_WORLDPOINT = new WorldPoint(2989, 3366, 0);
    private final WorldPoint MARLO_WORLDPOINT = new WorldPoint(3240, 3471, 0);
    private final WorldPoint ELLIE_WORLDPOINT = new WorldPoint(2636, 3293, 0);
    private final WorldPoint ANGELO_WORLDPOINT = new WorldPoint(1782, 3626, 0);
    private final String[] strs = {"Expert Contract (Requires 70 Construction)"};
    private final HashSet<String> l = new HashSet<>(Arrays.asList(strs));
    private void handle2379()
    {
        Widget dialogueOptionsWidget = client.getWidget(WidgetInfo.DIALOG_OPTION_OPTIONS);
        if (dialogueOptionsWidget == null || dialogueOptionsWidget.getChildren() == null)
            return;
        Widget[] options = dialogueOptionsWidget.getChildren();
        String text = options[lastAction].getText();
        if (!l.contains(text))
            return;
        if (lastAction != -1 && lastAction < options.length)
            if (client.getLocalPlayer() == null)
                return;
            WorldPoint wp = client.getLocalPlayer().getWorldLocation();
            String guessedName;
            if (lastNpcName == null)
            {
                // We need to take a guess here:
                if (wp.distanceTo(AMY_WORLDPOINT) < 10)
                    guessedName = "Amy";
                else if(wp.distanceTo(MARLO_WORLDPOINT) < 10)
                    guessedName = "Marlo";
                else if(wp.distanceTo(ELLIE_WORLDPOINT) < 10)
                    guessedName = "Ellie";
                else if(wp.distanceTo(ANGELO_WORLDPOINT) < 10)
                    guessedName = "Angelo";
                else
                    guessedName = "Amy via NPC contact";
            }
            else if("Amy".equals(lastNpcName))
            {
                if (wp.distanceTo(AMY_WORLDPOINT) < 10)
                    guessedName = "Amy";
                else
                    guessedName = "Amy via NPC contact";
            }
            else
                guessedName = lastNpcName;
            log.info("[GUESS] " + guessedName + ": " + options[lastAction].getText());
    }

    @Subscribe
    private void onScriptPreFired(ScriptPreFired event)
    {
        if (event.getScriptId() == 73) {
            Widget dialogueNpcName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME);
            if (dialogueNpcName == null)
                return;
            lastNpcName = dialogueNpcName.getText();
        }
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        if (widgetLoaded.getGroupId() != WidgetInfo.DIALOG_NPC_NAME.getGroupId())
            return;
        lastNpcName = null;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
        if (menuOptionClicked.getMenuAction() == MenuAction.WIDGET_TYPE_6 && menuOptionClicked.getMenuOption().equals("Continue")) {
            lastAction = menuOptionClicked.getParam0();
            handle2379();
        }
    }
}
