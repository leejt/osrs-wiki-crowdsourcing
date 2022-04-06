package com.Crowdsourcing.monster_examine;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Arrays;

@Singleton
class MonsterExamineOverlay extends Overlay
{

    private final Client client;
    private final MonsterExamine plugin;

    private static final Color PURPLE = new Color(170, 0, 255);

    @Inject
    private MonsterExamineOverlay(Client client, MonsterExamine plugin)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGHEST);
        this.client = client;
        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        for (NPC npc : client.getNpcs())
        {
            if (npc == null)
            {
                continue;
            }
            if (plugin.getSeenIds().contains(npc.getComposition().getId()))
            {
                continue;
            }

            NPCComposition composition = npc.getComposition();
            if (!Arrays.asList(composition.getActions()).contains("Attack"))
            {
                continue;
            }


            Shape objectClickbox = npc.getConvexHull();
            if (objectClickbox == null)
            {
                continue;
            }

            String text = "ID:" + composition.getId();
            Point textLocation = npc.getCanvasTextLocation(graphics, text, npc.getLogicalHeight() + 40);
            if (textLocation != null)
            {
                OverlayUtil.renderTextLocation(graphics, textLocation, text, PURPLE);
            }
            graphics.setColor(PURPLE);
            graphics.draw(objectClickbox);
            graphics.setColor(new Color(PURPLE.getRed(), PURPLE.getGreen(), PURPLE.getBlue(), 20));
            graphics.fill(objectClickbox);
        }
        return null;
    }
}