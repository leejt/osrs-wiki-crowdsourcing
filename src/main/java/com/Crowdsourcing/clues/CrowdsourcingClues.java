package com.Crowdsourcing.clues;


import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import static net.runelite.api.ItemID.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.crowdsourcing.CrowdsourcingManager;

@Slf4j
public class CrowdsourcingClues {

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private ItemManager itemManager;

	// Known MAP clues
	private static final Map<Integer, String> MAP_CLUES = new ImmutableMap.Builder<Integer, String>()
		.put(CLUE_SCROLL_EASY_12179, "Map - Al Kharid mine")
		.put(CLUE_SCROLL_EASY_2713, "Map - Champion Guild")
		.put(CLUE_SCROLL_EASY_2716, "Map - Varrock East Mines")
		.put(CLUE_SCROLL_EASY_2719, "Map - Standing Stones")
		.put(CLUE_SCROLL_EASY_3516, "Map - Brother Galahad's house.")
		.put(CLUE_SCROLL_EASY_3518, "Map - Wizard Tower DIS")
		.put(CLUE_SCROLL_EASY_7236, "Map - North of Falador.")
		.put(CLUE_SCROLL_MEDIUM_2827, "Map - South of Draynor bank")
		.put(CLUE_SCROLL_MEDIUM_3596, "Map - West of the Crafting Guild")
		.put(CLUE_SCROLL_MEDIUM_3598, "Map - McGrubor's Wood")
		.put(CLUE_SCROLL_MEDIUM_3599, "Map - North of the Tower of Life")
		.put(CLUE_SCROLL_MEDIUM_3601, "Map - Clocktower.")
		.put(CLUE_SCROLL_MEDIUM_3602, "Map - Chemist's house in Rimmington")
		.put(CLUE_SCROLL_MEDIUM_7286, "Map - Miscellania")
		.put(CLUE_SCROLL_MEDIUM_7288, "Map - Mort Myre Swamp, west of Mort'ton")
		.put(CLUE_SCROLL_MEDIUM_7290, "Map - Entrance to the Ourania Cave.")
		.put(CLUE_SCROLL_MEDIUM_7292, "Map - Lighthouse")
		.put(CLUE_SCROLL_MEDIUM_7294, "Map - Between Seers' Village and Rellekka")
		.put(CLUE_SCROLL_HARD, "Map - Lumber Yard")
		.put(CLUE_SCROLL_HARD_3520, "Map - Yanille anvils")
		.put(CLUE_SCROLL_HARD_3522, "Map - West Ardougne")
		.put(CLUE_SCROLL_HARD_3524, "Map - Observatory Dungeon.")
		.put(CLUE_SCROLL_HARD_3525, "Map - Dark Warriors' Fortress")
		.put(CLUE_SCROLL_HARD_7239, "Map - South-east of the Wilderness Agility Course")
		.put(CLUE_SCROLL_HARD_7241, "Map - South of the Legends' Guild")
		.put(CLUE_SCROLL_ELITE_12130, "Map - South-west of Tree Gnome Village.")
		.put(CLUE_SCROLL_ELITE_19782, "Map - In the Mogre Camp, near Port Khazard")
		.put(CLUE_SCROLL_ELITE_19783, "Map - Zul-Andra")
		.put(CLUE_SCROLL_ELITE_19784, "Map - At the Soul Altar")
		.put(CLUE_SCROLL_ELITE_19785, "Map - East of Burgh de Rott.")
		.put(CLUE_SCROLL_ELITE_19786, "Map - Ape Atoll")
		.build();

	private Integer previousClue = 0;
	private Integer clueId = -1;
	private String clueText;

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event) {
		if (event.getMenuOption() == null) {
			return;
		}

		if(MAP_CLUES.containsKey(event.getId()) || event.getId() == CLUE_SCROLL_BEGINNER || event.getId() == CLUE_SCROLL_MASTER) {
			return;
		}

		if (event.getMenuOption().equals("Read")) {
			final ItemComposition itemComposition = itemManager.getItemComposition(event.getId());

			if (itemComposition != null && (itemComposition.getName().startsWith("Clue scroll") || itemComposition.getName().startsWith("Challenge scroll"))) {
				clueId = itemComposition.getId();
				log.debug("Clue ID: {}, Tier: {}", clueId);
			}
		}
	}


	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (event.getGroupId() == WidgetID.CLUE_SCROLL_GROUP_ID && clueId != -1) {
			final Widget clueWidgetText = client.getWidget(WidgetInfo.CLUE_SCROLL_TEXT);
			clueText = clueWidgetText.getText();
		}

		if (clueText != "" && clueId != -1) {
			submitClue();
		}

	}

	private void submitClue() {
		if (clueId != previousClue) {
			previousClue = clueId;
			ClueData clue = new ClueData(clueId, clueText);
			manager.storeEvent(clue);
		}
		resetClue();
	}

	private void resetClue() {
		clueText = "";
		clueId = -1;
	}
}
