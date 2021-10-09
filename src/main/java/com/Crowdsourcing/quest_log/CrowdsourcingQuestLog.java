package com.Crowdsourcing.quest_log;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingQuestLog
{
	static class VarTuple {
		public final int varType;
		public final int varIndex;
		public VarTuple(int varType, int varIndex) {
			this.varType = varType;
			this.varIndex = varIndex;
		}
	}
	static HashMap<String, VarTuple> questVarbs = new HashMap<>();
	static
	{
		// Set varb or varp to send with each one
		questVarbs.put("Black Knights' Fortress", new VarTuple(1, 130));
		questVarbs.put("Cook's Assistant", new VarTuple(1, 29));
		questVarbs.put("The Corsair Curse", new VarTuple(0, 6071));
		questVarbs.put("Demon Slayer", new VarTuple(0, 2561));
		questVarbs.put("Doric's Quest", new VarTuple(1, 31));
		questVarbs.put("Dragon Slayer I", new VarTuple(1, 176));
		questVarbs.put("Ernest the Chicken", new VarTuple(1, 32));
		questVarbs.put("Goblin Diplomacy", new VarTuple(0, 2378));
		questVarbs.put("Imp Catcher", new VarTuple(1, 160));
		questVarbs.put("The Knight's Sword", new VarTuple(1, 122));
		questVarbs.put("Misthalin Mystery", new VarTuple(0, 3468));
		questVarbs.put("Pirate's Treasure", new VarTuple(1, 71));
		questVarbs.put("Prince Ali Rescue", new VarTuple(1, 273));
		questVarbs.put("The Restless Ghost", new VarTuple(1, 107));
		questVarbs.put("Romeo & Juliet", new VarTuple(1, 144));
		questVarbs.put("Rune Mysteries", new VarTuple(1, 63));
		questVarbs.put("Sheep Shearer", new VarTuple(1, 179));
		questVarbs.put("Vampyre Slayer", new VarTuple(1, 178));
		questVarbs.put("Witch's Potion", new VarTuple(1, 67));
		questVarbs.put("X Marks the Spot", new VarTuple(0, 8063));
		questVarbs.put("Below Ice Mountain", new VarTuple(0, 12063));
		questVarbs.put("Animal Magnetism", new VarTuple(0, 3185));
		questVarbs.put("Another Slice of H.A.M.", new VarTuple(0, 3550));
		questVarbs.put("Between a Rock...", new VarTuple(0, 299));
		questVarbs.put("Big Chompy Bird Hunting", new VarTuple(1, 293));
		questVarbs.put("Biohazard", new VarTuple(1, 68));
		questVarbs.put("Cabin Fever", new VarTuple(1, 655));
		questVarbs.put("Clock Tower", new VarTuple(1, 10));
		questVarbs.put("Cold War", new VarTuple(0, 3293));
		questVarbs.put("Contact!", new VarTuple(0, 3274));
		questVarbs.put("Creature of Fenkenstrain", new VarTuple(1, 399));
		questVarbs.put("Darkness of Hallowvale", new VarTuple(0, 2573));
		questVarbs.put("Death Plateau", new VarTuple(1, 314));
		questVarbs.put("Death to the Dorgeshuun", new VarTuple(0, 2258));
		questVarbs.put("The Depths of Despair", new VarTuple(0, 6027));
		questVarbs.put("Desert Treasure", new VarTuple(0, 358));
		questVarbs.put("Devious Minds", new VarTuple(0, 1465));
		questVarbs.put("The Dig Site", new VarTuple(1, 131));
		questVarbs.put("Dragon Slayer II", new VarTuple(0, 6104));
		questVarbs.put("Dream Mentor", new VarTuple(0, 3618));
		questVarbs.put("Druidic Ritual", new VarTuple(1, 80));
		questVarbs.put("Dwarf Cannon", new VarTuple(1, 0));
		questVarbs.put("Eadgar's Ruse", new VarTuple(1, 335));
		questVarbs.put("Eagles' Peak", new VarTuple(0, 2780));
		questVarbs.put("Elemental Workshop I", new VarTuple(1, 299));
		questVarbs.put("Elemental Workshop II", new VarTuple(0, 2639));
		questVarbs.put("Enakhra's Lament", new VarTuple(0, 1560));
		questVarbs.put("Enlightened Journey", new VarTuple(0, 2866));
		questVarbs.put("The Eyes of Glouphrie", new VarTuple(0, 2497));
		questVarbs.put("Fairytale I - Growing Pains", new VarTuple(0, 1803));
		questVarbs.put("Fairytale II - Cure a Queen", new VarTuple(0, 2326));
		questVarbs.put("Family Crest", new VarTuple(1, 148));
		questVarbs.put("The Feud", new VarTuple(0, 334));
		questVarbs.put("Fight Arena", new VarTuple(1, 17));
		questVarbs.put("Fishing Contest", new VarTuple(1, 11));
		questVarbs.put("Forgettable Tale...", new VarTuple(0, 822));
		questVarbs.put("Bone Voyage", new VarTuple(0, 5795));
		questVarbs.put("The Fremennik Isles", new VarTuple(0, 3311));
		questVarbs.put("The Fremennik Trials", new VarTuple(1, 347));
		questVarbs.put("Garden of Tranquillity", new VarTuple(0, 961));
		questVarbs.put("Gertrude's Cat", new VarTuple(1, 180));
		questVarbs.put("Ghosts Ahoy", new VarTuple(0, 217));
		questVarbs.put("The Giant Dwarf", new VarTuple(0, 571));
		questVarbs.put("The Golem", new VarTuple(0, 346));
		questVarbs.put("The Grand Tree", new VarTuple(1, 150));
		questVarbs.put("The Great Brain Robbery", new VarTuple(1, 980));
		questVarbs.put("Grim Tales", new VarTuple(0, 2783));
		questVarbs.put("The Hand in the Sand", new VarTuple(0, 1527));
		questVarbs.put("Haunted Mine", new VarTuple(1, 382));
		questVarbs.put("Hazeel Cult", new VarTuple(1, 223));
		questVarbs.put("Heroes' Quest", new VarTuple(1, 188));
		questVarbs.put("Holy Grail", new VarTuple(1, 5));
		questVarbs.put("Horror from the Deep", new VarTuple(0, 34));
		questVarbs.put("Icthlarin's Little Helper", new VarTuple(0, 418));
		questVarbs.put("In Aid of the Myreque", new VarTuple(0, 1990));
		questVarbs.put("In Search of the Myreque", new VarTuple(1, 387));
		questVarbs.put("Jungle Potion", new VarTuple(1, 175));
		questVarbs.put("King's Ransom", new VarTuple(0, 3888));
		questVarbs.put("Legends' Quest", new VarTuple(1, 139));
		questVarbs.put("Lost City", new VarTuple(1, 147));
		questVarbs.put("The Lost Tribe", new VarTuple(0, 532));
		questVarbs.put("Lunar Diplomacy", new VarTuple(0, 2448));
		questVarbs.put("Making Friends with My Arm", new VarTuple(0, 6528));
		questVarbs.put("Making History", new VarTuple(0, 1383));
		questVarbs.put("Merlin's Crystal", new VarTuple(1, 14));
		questVarbs.put("Monkey Madness I", new VarTuple(1, 365));
		questVarbs.put("Monkey Madness II", new VarTuple(0, 5027));
		questVarbs.put("Monk's Friend", new VarTuple(1, 30));
		questVarbs.put("Mountain Daughter", new VarTuple(0, 260));
		questVarbs.put("Mourning's End Part I", new VarTuple(1, 517));
		questVarbs.put("Mourning's End Part II", new VarTuple(0, 1103));
		questVarbs.put("Murder Mystery", new VarTuple(1, 192));
		questVarbs.put("My Arm's Big Adventure", new VarTuple(0, 2790));
		questVarbs.put("Nature Spirit", new VarTuple(1, 307));
		questVarbs.put("Observatory Quest", new VarTuple(1, 112));
		questVarbs.put("Olaf's Quest", new VarTuple(0, 3534));
		questVarbs.put("One Small Favour", new VarTuple(1, 416));
		questVarbs.put("Plague City", new VarTuple(1, 165));
		questVarbs.put("Priest in Peril", new VarTuple(1, 302));
		questVarbs.put("The Queen of Thieves", new VarTuple(0, 6037));
		questVarbs.put("Rag and Bone Man I", new VarTuple(1, 714));
		questVarbs.put("Rag and Bone Man II", new VarTuple(1, 714));
		questVarbs.put("Ratcatchers", new VarTuple(0, 1404));
		questVarbs.put("Recipe for Disaster", new VarTuple(0, 1850));
		questVarbs.put("Recruitment Drive", new VarTuple(0, 657));
		questVarbs.put("Regicide", new VarTuple(1, 328));
		questVarbs.put("Roving Elves", new VarTuple(1, 402));
		questVarbs.put("Royal Trouble", new VarTuple(0, 2140));
		questVarbs.put("Rum Deal", new VarTuple(1, 600));
		questVarbs.put("Scorpion Catcher", new VarTuple(1, 76));
		questVarbs.put("Sea Slug", new VarTuple(1, 159));
		questVarbs.put("Shades of Mort'ton", new VarTuple(1, 339));
		questVarbs.put("Shadow of the Storm", new VarTuple(0, 1372));
		questVarbs.put("Sheep Herder", new VarTuple(1, 60));
		questVarbs.put("Shilo Village", new VarTuple(1, 116));
		questVarbs.put("The Slug Menace", new VarTuple(0, 2610));
		questVarbs.put("A Soul's Bane", new VarTuple(0, 2011));
		questVarbs.put("Spirits of the Elid", new VarTuple(0, 1444));
		questVarbs.put("Swan Song", new VarTuple(0, 2098));
		questVarbs.put("Tai Bwo Wannai Trio", new VarTuple(1, 320));
		questVarbs.put("A Tail of Two Cats", new VarTuple(0, 1028));
		questVarbs.put("Tale of the Righteous", new VarTuple(0, 6358));
		questVarbs.put("A Taste of Hope", new VarTuple(0, 6396));
		questVarbs.put("Tears of Guthix", new VarTuple(0, 451));
		questVarbs.put("Temple of Ikov", new VarTuple(1, 26));
		questVarbs.put("Throne of Miscellania", new VarTuple(1, 359));
		questVarbs.put("The Tourist Trap", new VarTuple(1, 197));
		questVarbs.put("Tower of Life", new VarTuple(0, 3337));
		questVarbs.put("Tree Gnome Village", new VarTuple(1, 111));
		questVarbs.put("Tribal Totem", new VarTuple(1, 200));
		questVarbs.put("Troll Romance", new VarTuple(1, 385));
		questVarbs.put("Troll Stronghold", new VarTuple(1, 317));
		questVarbs.put("Underground Pass", new VarTuple(1, 161));
		questVarbs.put("Client of Kourend", new VarTuple(0, 5619));
		questVarbs.put("Wanted!", new VarTuple(0, 1051));
		questVarbs.put("Watchtower", new VarTuple(1, 212));
		questVarbs.put("Waterfall Quest", new VarTuple(1, 65));
		questVarbs.put("What Lies Below", new VarTuple(0, 3523));
		questVarbs.put("Witch's House", new VarTuple(1, 226));
		questVarbs.put("Zogre Flesh Eaters", new VarTuple(0, 487));
		questVarbs.put("The Ascent of Arceuus", new VarTuple(0, 7856));
		questVarbs.put("The Forsaken Tower", new VarTuple(0, 7796));
		questVarbs.put("Song of the Elves", new VarTuple(0, 9016));
		questVarbs.put("The Fremennik Exiles", new VarTuple(0, 9459));
		questVarbs.put("Sins of the Father", new VarTuple(0, 7255));
		questVarbs.put("A Porcine of Interest", new VarTuple(0, 10582));
		questVarbs.put("Getting Ahead", new VarTuple(0, 693));
		questVarbs.put("A Kingdom Divided", new VarTuple(0, 12296));
		questVarbs.put("A Night at the Theatre", new VarTuple(0, 12276));
	}

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event)
	{
		// Only check if the diary/quest widget text is changing
		if (event.getGroupId() != WidgetInfo.DIARY_QUEST_WIDGET_TEXT.getGroupId())
			return;
		Widget w = client.getWidget(WidgetInfo.DIARY_QUEST_WIDGET_TEXT);
		if (w == null)
			return;

		// Get all of the lines of the text area. Each line is a widget.
		Widget[] children = w.getStaticChildren();
		if (children == null)
			return;

		// Concat the strs and append a newline between each widget (since they are on different lines
		StringBuilder s = new StringBuilder();
		if (w.getStaticChildren() != null) {
			for(Widget child : children) {
				s.append(child.getText());
				s.append('\n');
			}
		}

		// Get the title and figure out if this is a quest.
		Widget titleWidget = client.getWidget(WidgetInfo.DIARY_QUEST_WIDGET_TITLE);
		if (titleWidget == null || titleWidget.getText() == null)
			return;
		String key = titleWidget.getText().substring("<col=7f0000>".length(), titleWidget.getText().length()-6);
		if (CrowdsourcingQuestLog.questVarbs.containsKey(key))
		{
			// If this is a quest other than SOA, send in data for the main quest progress var.
			log.debug("Matched! " + key);
			VarTuple tup = questVarbs.get(key);
			boolean isVarbit = tup.varType == 0;
			int varValue = isVarbit ? client.getVarbitValue(tup.varIndex) : client.getVarpValue(tup.varIndex);
			QuestLogData data = new QuestLogData(key, isVarbit, tup.varIndex, varValue, s.toString().trim());
			manager.storeEvent(data);
		}
		else if ("Shield of Arrav".equals(key))
		{
			// If this is SOA, we have two varps to send data for.
			log.debug("Matched! " + key);
			QuestLogData data = new QuestLogData(key, false, 145, client.getVarpValue(145), s.toString().trim());
			QuestLogData data2 = new QuestLogData(key, false, 146, client.getVarpValue(146), s.toString().trim());
			manager.storeEvent(data);
			manager.storeEvent(data2);
		}
		else {
			log.debug("No match! " + key);
		}
	}

}
