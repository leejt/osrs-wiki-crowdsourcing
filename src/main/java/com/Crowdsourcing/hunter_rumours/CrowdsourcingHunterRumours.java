package com.Crowdsourcing.hunter_rumours;

import com.Crowdsourcing.CrowdsourcingManager;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CrowdsourcingHunterRumours {
    @Inject
    private CrowdsourcingManager manager;

    @Inject
    private Client client;

    private static final String FALCONRY_KEBBITS = "falconry kebbits";
    private static final String BUTTERFLY_OR_MOTH = "butterfly or moth";
    private static final String RARE_PART_FOUND_TEXT = "You find a rare piece of the creature!";
    private static final Map<String, String> messageCreatureMap = new HashMap<>() {{
        //distinct messages
        put("You've caught a tropical wagtail.","tropical wagtail");
        put("You've caught a wild kebbit.","wild kebbit");
        put("You've caught a prickly kebbit.","prickly kebbit");
        put("You've caught a barb-tailed kebbit.","barb-tailed kebbit");
        put("You've caught a sabre-toothed kebbit.","sabre-toothed kebbit");
        put("You've caught a pyre fox.","pyre fox");
        put("You've caught a swamp lizard.","swamp lizard");
        put("You've caught an orange salamander.","orange salamander");
        put("You've caught a red salamander.","red salamander");
        put("The salamander that pops out of the net doesn't look to be fully grown.","tecu salamander");
        put("The salamander is fully grown, you could use this as a weapon.","tecu salamander");
        put("You've caught an embertailed jerboa... or maybe not.","embertailed jerboa");
        put("You've caught a chinchompa.","grey chinchompa"); //message excludes "grey"
        put("You've caught a carnivorous chinchompa.","carnivorous chinchompa");
        put("You've caught a spined larupia!","spined larupia");
        put("You've caught a horned graahk!","horned graahk");
        put("You've caught a sabretoothed kyatt!","sabre-toothed kyatt"); //message excludes "-"
        put("You've caught a sunlight antelope!","sunlight antelope");
        put("You've caught a moonlight antelope!","moonlight antelope");
        put("You manage to noose a razor-backed kebbit that is hiding in the bush.","razor-backed kebbit");
        put("You harvest herbs from the herbiboar, whereupon it escapes.", "herbiboar");
        put("You release the sapphire glacialis butterfly.","sapphire glacialis"); //no jars
        put("You catch and release the snowy knight butterfly.","snowy knight"); //no jars
        put("You catch and release the black warlock butterfly.", "black warlock"); //no jars
        put("You release the sunlight moth.","sunlight moth"); //no jars
        put("You catch and release the moonlight moth.","moonlight moth"); //no jars

        //ambiguous messages
        put("You retrieve the falcon as well as the fur of the dead kebbit.", FALCONRY_KEBBITS); //falconry
        put("You manage to catch the butterfly and place it in a jar.", BUTTERFLY_OR_MOTH); //with net and jars
        // NOTE: If catching butterflies/moths barehanded WITH jars, then NO message gets sent to chat box.
    }};

    private static final int HUNTER_GUILD_BASEMENT_ID = 6291;
    private static final String GILMAN = "Huntmaster Gilman (Novice)";
    private static final String CERVUS = "Guild Hunter Cervus (Adept)";
    private static final String ORNUS = "Guild Hunter Ornus (Adept)";
    private static final String ACO = "Guild Hunter Aco (Expert)";
    private static final String TECO = "Guild Hunter Teco (Expert)";
    private static final String WOLF = "Guild Hunter Wolf (Master)";
    private static final Pattern GILMAN_PATTERN = Pattern.compile(
            "That was it\\. I asked you to hunt (?:a|an)? ([a-zA-Z -]+) and");
    private static final Pattern CERVUS_PATTERN = Pattern.compile(
            "Ah, yes\\. I asked you to hunt (?:a|an) ([a-zA-Z -]+) and");
    private static final Pattern ORNUS_PATTERN = Pattern.compile(
            "Oh, I remember! I asked you to hunt (?:a|an) ([a-zA-Z -]+) and");
    private static final Pattern ACO_PATTERN = Pattern.compile(
            "Now then\\.\\.\\. I asked you to hunt (?:a|an) ([a-zA-Z -]+) for me");
    private static final Pattern TECO_PATTERN = Pattern.compile(
            "I asked you to hunt (?:a|an) ([a-zA-Z -]+) for me");
    private static final Pattern WOLF_PATTERN = Pattern.compile(
            "If I'm remembering correctly, I asked you to hunt (?:a|an) ([a-zA-Z -]+) and");
    private static final Map<String, Pattern> hunterPatternMap = new HashMap<>() {{
        put(GILMAN, GILMAN_PATTERN);
        put(CERVUS, CERVUS_PATTERN);
        put(ORNUS, ORNUS_PATTERN);
        put(ACO, ACO_PATTERN);
        put(TECO, TECO_PATTERN);
        put(WOLF, WOLF_PATTERN);
    }};
    private static final String[] GENERIC_RUMOUR_REGEX_STRINGS = {
            "Someone thinks they've seen (?:a|an) ([a-zA-Z -]+)\\.",
            "There's been rumours of a rare ([a-zA-Z -]+) coming in\\.",
            "We've got reports of a slightly different ([a-zA-Z -]+) in the local population\\.",
            "Looks like we need some information on a unique specimen amongst the local ([a-zA-Z -]+) population\\.",
            "I've heard rumours of a strange ([a-zA-Z -]+) that you could check out for me\\."};

    private boolean partFound = false;
    private String creatureFromMessage = null;
    private String currRumourCreature = "";
    private String currRumourHunter = "";
    private int prevHunterXP = -1;
    private int xpGained = 0;
    private int rumourKC = 0;

    @Subscribe
    public void onGameTick(GameTick gameTick) {
        if (creatureFromMessage != null) {
            if (creatureFromMessage.equals(FALCONRY_KEBBITS) || creatureFromMessage.equals(BUTTERFLY_OR_MOTH)) {
                creatureFromMessage = creatureFromXP(xpGained);
            }
            if (currRumourCreature.equals(creatureFromMessage)) {
                rumourKC++;
                log.debug("\nRUMOUR KC={}", rumourKC);
                if (partFound) {
                    HunterRumourData data = new HunterRumourData(currRumourHunter, currRumourCreature, rumourKC);
                    manager.storeEvent(data);
                    log.debug("\n==SENT HUNTER DATA==\nHunter={}\nCreature={}\nkc={}", currRumourHunter, currRumourCreature, rumourKC);
                    currRumourCreature = "";
                    currRumourHunter = "";
                    rumourKC = 0;
                    partFound = false;
                }
            }
        }
        creatureFromMessage = null;
        xpGained = 0;

        if (client.getLocalPlayer().getWorldLocation().getRegionID() != HUNTER_GUILD_BASEMENT_ID) {
            return;
        }

        Widget npcTextWidget = client.getWidget(ComponentID.DIALOG_NPC_TEXT);
        if (npcTextWidget == null) {
            return;
        }

        String text = npcTextWidget.getText().replace("<br>", " ");
        String npcName = client.getWidget(ComponentID.DIALOG_NPC_NAME).getText();
        Pattern pattern = hunterPatternMap.get(npcName);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            currRumourHunter = npcName;
            currRumourCreature = matcher.group(1);
            log.debug("\nUnique Dialogue found rumour:\nHunter={}\nCreature={}", currRumourHunter, currRumourCreature);
        }

        for (String regexStr : GENERIC_RUMOUR_REGEX_STRINGS) {
            Pattern patternGeneric = Pattern.compile(regexStr);
            Matcher matcherGeneric = patternGeneric.matcher(text);
            if (matcherGeneric.find()) {
                currRumourHunter = npcName;
                currRumourCreature = matcherGeneric.group(1);
                log.debug("\nGeneric Dialogue found rumour:\nHunter={}\nCreature={}", currRumourHunter, currRumourCreature);
                break;
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM) {
            return;
        }

        String message = chatMessage.getMessage();
        if (messageCreatureMap.containsKey(message)) {
            creatureFromMessage = messageCreatureMap.get(message);
        }

        if (message.contains(RARE_PART_FOUND_TEXT)) {
            partFound = true;
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        if (statChanged.getSkill() != Skill.HUNTER) {
            return;
        }

        final int xp = statChanged.getXp();
        if (prevHunterXP != -1) {
            xpGained = xp - prevHunterXP;
        }
        prevHunterXP = xp;
    }

    private String creatureFromXP(int xp) {
        if (xp == 34) return "sapphire glacialis";
        if (xp == 44) return "snowy knight";
        if (xp == 54) return "black warlock";
        if (xp == 74) return "sunlight moth";
        if (xp == 84) return "moonlight moth";
        if (xp == 104) return "spotted kebbit";
        if (xp == 132) return "dark kebbit";
        if (xp == 156) return "dashing kebbit";
        return null;
    }
}