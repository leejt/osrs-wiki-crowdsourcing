package com.Crowdsourcing;

import javax.inject.Inject;

import net.runelite.api.AnimationID;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;


import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingCooking {
    private static final String COOKING_FIRE = "fire";
    private static final String COOKING_RANGE = "range";

    private static final int HOSIDIUS_KITCHEN_REGION = 6712;

    @Inject
    private CrowdsourcingManager manager;

    @Inject
    private Client client;

    private String lastCookingAnimation;

    private boolean hasCookingGauntlets()
    {
        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipmentContainer == null)
        {
            return false;
        }

        Item[] items = equipmentContainer.getItems();
        int idx = EquipmentInventorySlot.GLOVES.getSlotIdx();

        if (items == null || idx >= items.length)
        {
            return false;
        }

        Item glove = items[idx];
        return glove != null && glove.getId() == ItemID.COOKING_GAUNTLETS;
    }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event)
    {
        Player local = client.getLocalPlayer();

        if (event.getActor() != local)
        {
            return;
        }
        // This is -1 for the first cook. Still unclear how to fix.
        int animId = local.getAnimation();
        if (animId == AnimationID.COOKING_FIRE) {
            lastCookingAnimation = COOKING_FIRE;
        } else if (animId == AnimationID.COOKING_RANGE) {
            lastCookingAnimation = COOKING_RANGE;
        }

    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.SPAM)
        {
            return;
        }

        final String message = event.getMessage();
        // Message prefixes taken from CookingPlugin
        if (message.startsWith("You successfully cook")
                || message.startsWith("You successfully bake")
                || message.startsWith("You manage to cook")
                || message.startsWith("You roast a")
                || message.startsWith("You cook")
                || message.startsWith("You accidentally burn")
                || message.startsWith("You accidentally spoil"))
        {
            boolean inHosidiusKitchen = false;
            Player local = client.getLocalPlayer();
            if (local != null && local.getWorldLocation().getRegionID() == HOSIDIUS_KITCHEN_REGION) {
                inHosidiusKitchen = true;
            }

            // TODO: Lumbridge range

            int cookingLevel = client.getBoostedSkillLevel(Skill.COOKING);
            boolean hasCookingGauntlets = hasCookingGauntlets();
            boolean kourendElite = client.getVar(Varbits.DIARY_KOUREND_ELITE) == 1;
            CrowdsourcingCookingData data = new CrowdsourcingCookingData(message, hasCookingGauntlets, inHosidiusKitchen, kourendElite, lastCookingAnimation, cookingLevel);
            manager.storeEvent(data);
        }
    }
}
