package com.Crowdsourcing.nex;

import com.Crowdsourcing.CrowdsourcingManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrowdsourcingNex {

    @Inject
    private Client client;

    @Inject
    public CrowdsourcingManager manager;

    static final List<Skill> skills = Arrays.asList(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
            Skill.RANGED, Skill.MAGIC);
    static final int VERSION_NUMBER = 2;
    List<NexDataEntry> session = new ArrayList<>();
    boolean inChamber = false;
    int[] skillLevels = new int[skills.size()];
    int[] skillXp = new int[skills.size()];
    int oldBossHp = 0;
    int oldAttackStyle = 0;
    int oldSpecEnergy = 0;
    int oldActivePrayers = 0;

    private void addData(Object data) {
        session.add(new NexDataEntry(client.getTickCount(), data));
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (client == null || client.getLocalPlayer() == null)
        {
            return;
        }
        LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
        if (local == null)
        {
            return;
        }
        WorldPoint playerLocation = WorldPoint.fromLocalInstance(client, local);
        if (playerLocation.getX() >= 2909 && playerLocation.getX() <= 2943
        && playerLocation.getY() >= 5188 && playerLocation.getY() <= 5218) {
            inChamber = true;
        } else {
            resetSession();
            inChamber = false;
        }
    }

    public void resetSession() {
        if (session.size() > 0) {
            manager.storeEvent(new NexData(session, VERSION_NUMBER));
            session = new ArrayList<>();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
        if (!inChamber) {
            return;
        }
        if (itemContainerChanged.getItemContainer() != client.getItemContainer(InventoryID.EQUIPMENT)) {
            return;
        }
        addData(itemContainerChanged.getItemContainer().getItems().clone());
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged)
    {
        if (!inChamber) {
            return;
        }
        Skill skill = statChanged.getSkill();
        int index = skills.indexOf(skill);
        if (index != -1) {
            int newXp = statChanged.getXp();
            int newBoostedLevel = statChanged.getBoostedLevel();
            if (newXp != skillXp[index]) {
                // XP drop
                Actor interacting = client.getLocalPlayer().getInteracting();
                if (interacting instanceof NPC) {
                    NPC npc = (NPC) interacting;
                    addData(new XPDropData(skill, newXp - skillXp[index],
                            npc.getId(), npc.getHealthRatio(), npc.getHealthScale()));
                }
                skillXp[index] = newXp;
            }
            if (newBoostedLevel != skillLevels[index]) {
                // Level change
                addData((index << 16) + statChanged.getBoostedLevel());
                skillLevels[index] = newBoostedLevel;
            }
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
    {
        if (!inChamber) {
            return;
        }

        Hitsplat.HitsplatType type = hitsplatApplied.getHitsplat().getHitsplatType();
        if (type != Hitsplat.HitsplatType.BLOCK_ME && type != Hitsplat.HitsplatType.BLOCK_OTHER
        && type != Hitsplat.HitsplatType.DAMAGE_ME && type != Hitsplat.HitsplatType.DAMAGE_OTHER
        && type != Hitsplat.HitsplatType.HEAL) {
            return;
        }

        Actor actor = hitsplatApplied.getActor();

        if (actor instanceof NPC)
        {
            NPC npc = (NPC) actor;
            addData(new HitsplatData(npc.getId(), type, hitsplatApplied.getHitsplat().getAmount(),
                    npc.getHealthRatio(), npc.getHealthScale()));
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (!inChamber) {
            return;
        }
        // BOSS_HP varbit
        int newBossHp = client.getVarbitValue(6099);
        if (newBossHp != oldBossHp) {
            addData(10000000 + newBossHp);
            oldBossHp = newBossHp;
        }

        int newAttackStyle = client.getVar(VarPlayer.ATTACK_STYLE);
        if (newAttackStyle != oldAttackStyle) {
            addData(20000000 + newAttackStyle);
            oldAttackStyle = newAttackStyle;
        }

        int newSpecEnergy = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
        if (newSpecEnergy != oldSpecEnergy) {
            addData(30000000 + newSpecEnergy);
            oldSpecEnergy = newSpecEnergy;
        }

        int newActivePrayers = client.getVarbitValue(4101);
        if (newActivePrayers != oldActivePrayers) {
            addData(new PrayerData(newActivePrayers));
            oldActivePrayers = newActivePrayers;
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        if (!inChamber) {
            return;
        }
        Actor actor = event.getActor();
        if (actor instanceof Player) {
            Player player = (Player) actor;
            boolean isMe = actor == client.getLocalPlayer();
            int animationId = player.getAnimation();
            if (isMe || animationId == 7643 || animationId == 1378) {
                // BGS and DWH
                addData(new AnimationData(isMe, animationId));
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        System.out.println(chatMessage);


        if (!inChamber) {
            return;
        }
        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM) {
            return;
        }
        String message = chatMessage.getMessage();
        if (message.startsWith("Nex: ")) {
            if (message.contains("Fill my soul with smoke!")) {
                resetSession();
                for (int i = 0; i < skills.size(); i++) {
                    skillLevels[i] = client.getBoostedSkillLevel(skills.get(i));
                    skillXp[i] = client.getSkillExperience(skills.get(i));
                }
                addData(skillLevels.clone());
                addData(skillXp.clone());
                ItemContainer equipContainer = client.getItemContainer(InventoryID.EQUIPMENT);
                if (equipContainer != null) {
                    addData(equipContainer.getItems());
                }
                oldAttackStyle = client.getVar(VarPlayer.ATTACK_STYLE);
                addData(20000000 + oldAttackStyle);
                oldSpecEnergy = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);
                addData(30000000 + oldSpecEnergy);
                oldActivePrayers = client.getVarbitValue(4101);
                addData(new PrayerData(oldActivePrayers));

            }
            addData(message);
        }
    }
}
