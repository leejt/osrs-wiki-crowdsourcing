package com.Crowdsourcing.mahogany_homes;

import com.google.common.collect.ImmutableSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import java.util.HashSet;
import net.runelite.client.util.Text;

@Slf4j
public class CrowdsourcingMahoganyHomes {

    @Inject
    public Client client;

    private static int lastAction = -1;
    private String contractorName = null;

    private final String[] strs = {"Expert Contract (Requires 70 Construction)"};

    @Subscribe
    private void onScriptPreFired(ScriptPreFired event)
    {
        if (event.getScriptId() == 73) {
            Widget dialogueNpcName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME);
            if (dialogueNpcName == null)
                return;
            contractorName = dialogueNpcName.getText();
			log.error("Stored npc name: " + contractorName);
        }
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        if (widgetLoaded.getGroupId() != WidgetInfo.DIALOG_NPC_NAME.getGroupId())
            return;
        contractorName = null;
    }

	private static final String CONTACT_STRING = "NPC Contact";
	private static final ImmutableSet<String> NPC_NAMES = ImmutableSet.<String>builder()
		.add("Amy").add("Angelo").add("Ellie").add("Marlo").build();

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
		if (menuOptionClicked.getMenuAction().equals(MenuAction.CC_OP))
		{
			if (Text.removeTags(menuOptionClicked.getMenuTarget()).equals(CONTACT_STRING))
			{
				contractorName = "Amy";
				log.error("Stored npc: " + contractorName);
			}
		}
		if (menuOptionClicked.getMenuAction().equals(MenuAction.NPC_FIRST_OPTION) ||
				menuOptionClicked.getMenuAction().equals(MenuAction.NPC_THIRD_OPTION))
		{
			String target = Text.removeTags(menuOptionClicked.getMenuTarget());

			if (NPC_NAMES.contains(target) && !target.equals(contractorName))
			{
				contractorName = target;
				log.error("Stored npc: " + contractorName);
			}
		}
    }

	// Yoinked and adapted this function from https://github.com/TheStonedTurtle/Mahogany-Homes/blob/a9e118c1a07df4f4bd07e259ec23b6bef5f26206/src/main/java/thestonedturtle/mahoganyhomes/MahoganyHomesPlugin.java
	// Check for NPC dialog assigning or reminding us of a contract
	static final Pattern CONTRACT_PATTERN = Pattern.compile("(Please could you g|G)o see (\\w*)[ ,][\\w\\s,-]*[?.] You can get another job once you have furnished \\w* home\\.");
	private static final ImmutableSet<String> HOMEOWNER_NAMES = ImmutableSet.<String>builder()
		.add("Jess").add("Noella").add("Ross").add("Larry").add("Norman").add("Tau").add("Barbara").add("Leela")
		.add("Mariah").add("Bob").add("Jeff").add("Sarah").build();
	private String currentHomeowner = null;

	@Subscribe
	private void onGameTick(GameTick tick)
	{
		final Widget dialog = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
		if (dialog == null)
		{
			return;
		}

		final String npcText = Text.sanitizeMultilineText(dialog.getText());
		final Matcher startContractMatcher = CONTRACT_PATTERN.matcher(npcText);
		if (startContractMatcher.matches())
		{
			final String name = startContractMatcher.group(2);
			if (HOMEOWNER_NAMES.contains(name) && name.equals(currentHomeowner))
			{
				currentHomeowner = name;
				log.error("Home owner name set to: " + currentHomeowner);
				log.error("This task was given by: " + contractorName);
			}
		}
	}
}
