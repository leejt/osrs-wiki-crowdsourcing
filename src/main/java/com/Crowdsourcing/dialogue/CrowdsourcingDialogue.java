package com.Crowdsourcing.dialogue;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingDialogue
{
	private static final String USERNAME_TOKEN = "%USERNAME%";

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	private String lastNpcDialogueText = null;
	private String lastPlayerDialogueText = null;
	private Widget[] dialogueOptions;

	private String sanitize(String dialogue)  {
		String username = client.getLocalPlayer().getName();
		return dialogue.replaceAll(username, USERNAME_TOKEN);

	}
	@Subscribe
	public void onGameTick(GameTick tick) {
		Widget npcDialogueTextWidget = client.getWidget(WidgetInfo.DIALOG_NPC_TEXT);
		if (npcDialogueTextWidget != null && !npcDialogueTextWidget.getText().equals(lastNpcDialogueText)) {
			lastNpcDialogueText = npcDialogueTextWidget.getText();
			String npcName = client.getWidget(WidgetInfo.DIALOG_NPC_NAME).getText();
			NpcDialogueData data = new NpcDialogueData(sanitize(lastNpcDialogueText), npcName);
			manager.storeEvent(data);

		}

		Widget playerDialogueTextWidget = client.getWidget(WidgetID.DIALOG_PLAYER_GROUP_ID, 4);
		if (playerDialogueTextWidget != null && !playerDialogueTextWidget.getText().equals(lastPlayerDialogueText)) {
			lastPlayerDialogueText = playerDialogueTextWidget.getText();
			PlayerDialogueData data = new PlayerDialogueData(sanitize(lastPlayerDialogueText));
			manager.storeEvent(data);
		}

		Widget playerDialogueOptionsWidget = client.getWidget(WidgetID.DIALOG_OPTION_GROUP_ID, 1);
		if (playerDialogueOptionsWidget != null && !playerDialogueOptionsWidget.getChildren().equals(dialogueOptions)) {
			dialogueOptions = playerDialogueOptionsWidget.getChildren();
			String[] optionsText = new String[dialogueOptions.length];
			for (int i = 0; i < dialogueOptions.length; i++) {
				optionsText[i] = sanitize(dialogueOptions[i].getText());
			}
			DialogueOptionsData data = new DialogueOptionsData(optionsText);
			manager.storeEvent(data);
			// submit
		}
	}
}
