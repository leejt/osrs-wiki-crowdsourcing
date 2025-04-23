package com.Crowdsourcing.quest_log;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.HashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
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
		questVarbs.put("Black Knights' Fortress", new VarTuple(1, VarPlayerID.SPY));
		questVarbs.put("Cook's Assistant", new VarTuple(1, VarPlayerID.COOKQUEST));
		questVarbs.put("The Corsair Curse", new VarTuple(0, VarbitID.CORSCURS_PROGRESS));
		questVarbs.put("Demon Slayer", new VarTuple(0, VarbitID.DEMONSLAYER_MAIN));
		questVarbs.put("Doric's Quest", new VarTuple(1, VarPlayerID.DORICQUEST));
		questVarbs.put("Dragon Slayer I", new VarTuple(1, VarPlayerID.DRAGONQUEST));
		questVarbs.put("Ernest the Chicken", new VarTuple(1, VarPlayerID.HAUNTED));
		questVarbs.put("Goblin Diplomacy", new VarTuple(0, VarbitID.GOBDIP_MAIN));
		questVarbs.put("Imp Catcher", new VarTuple(1, VarPlayerID.IMP));
		questVarbs.put("The Knight's Sword", new VarTuple(1, VarPlayerID.SQUIRE));
		questVarbs.put("Misthalin Mystery", new VarTuple(0, VarbitID.MISTMYST_PROGRESS));
		questVarbs.put("Pirate's Treasure", new VarTuple(1, VarPlayerID.HUNT));
		questVarbs.put("Prince Ali Rescue", new VarTuple(1, VarPlayerID.PRINCEQUEST));
		questVarbs.put("The Restless Ghost", new VarTuple(1, VarPlayerID.PRIESTSTART));
		questVarbs.put("Romeo & Juliet", new VarTuple(1, VarPlayerID.RJQUEST));
		questVarbs.put("Rune Mysteries", new VarTuple(1, VarPlayerID.RUNEMYSTERIES));
		questVarbs.put("Sheep Shearer", new VarTuple(1, VarPlayerID.SHEEP));
		questVarbs.put("Vampyre Slayer", new VarTuple(1, VarPlayerID.VAMPIRE));
		questVarbs.put("Witch's Potion", new VarTuple(1, VarPlayerID.HETTY));
		questVarbs.put("X Marks the Spot", new VarTuple(0, VarbitID.CLUEQUEST));
		questVarbs.put("Below Ice Mountain", new VarTuple(0, VarbitID.BIM));
		questVarbs.put("Animal Magnetism", new VarTuple(0, VarbitID.ANMA_MAIN));
		questVarbs.put("Another Slice of H.A.M.", new VarTuple(0, VarbitID.SLICE_QUEST));
		questVarbs.put("Between a Rock...", new VarTuple(0, VarbitID.DWARFROCK_QUEST));
		questVarbs.put("Big Chompy Bird Hunting", new VarTuple(1, VarPlayerID.CHOMPYBIRD));
		questVarbs.put("Biohazard", new VarTuple(1, VarPlayerID.BIOHAZARD));
		questVarbs.put("Cabin Fever", new VarTuple(1, VarPlayerID.FEVER_QUEST));
		questVarbs.put("Clock Tower", new VarTuple(1, VarPlayerID.COGQUEST));
		questVarbs.put("Cold War", new VarTuple(0, VarbitID.PENG_QUEST));
		questVarbs.put("Contact!", new VarTuple(0, VarbitID.CONTACT));
		questVarbs.put("Creature of Fenkenstrain", new VarTuple(1, VarPlayerID.FENK_QUEST));
		questVarbs.put("Darkness of Hallowvale", new VarTuple(0, VarbitID.MYQ3_MAIN_QUEST));
		questVarbs.put("Death Plateau", new VarTuple(1, VarPlayerID.DEATH_EQUIPROOM));
		questVarbs.put("Death to the Dorgeshuun", new VarTuple(0, VarbitID.DTTD_MAIN));
		questVarbs.put("The Depths of Despair", new VarTuple(0, VarbitID.HOSIDIUSQUEST));
		questVarbs.put("Desert Treasure", new VarTuple(0, VarbitID.DESERTTREASURE));
		questVarbs.put("Devious Minds", new VarTuple(0, VarbitID.DEVIOUS_MAIN));
		questVarbs.put("The Dig Site", new VarTuple(1, VarPlayerID.ITEXAMLEVEL));
		questVarbs.put("Dragon Slayer II", new VarTuple(0, VarbitID.DS2));
		questVarbs.put("Dream Mentor", new VarTuple(0, VarbitID.DREAM_PROG));
		questVarbs.put("Druidic Ritual", new VarTuple(1, VarPlayerID.DRUIDQUEST));
		questVarbs.put("Dwarf Cannon", new VarTuple(1, VarPlayerID.MCANNON));
		questVarbs.put("Eadgar's Ruse", new VarTuple(1, VarPlayerID.EADGAR_QUEST));
		questVarbs.put("Eagles' Peak", new VarTuple(0, VarbitID.EAGLEPEAK_QUEST));
		questVarbs.put("Elemental Workshop I", new VarTuple(1, VarPlayerID.ELEMENTAL_WORKSHOP_BITS));
		questVarbs.put("Elemental Workshop II", new VarTuple(0, VarbitID.ELEMENTAL_QUEST_2_MAIN));
		questVarbs.put("Enakhra's Lament", new VarTuple(0, VarbitID.ENAKH_QUEST));
		questVarbs.put("Enlightened Journey", new VarTuple(0, VarbitID.ZEP_QUEST));
		questVarbs.put("The Eyes of Glouphrie", new VarTuple(0, VarbitID.EYEGLO_QUEST));
		questVarbs.put("Fairytale I - Growing Pains", new VarTuple(0, VarbitID.FAIRY_FARMERS_QUEST));
		questVarbs.put("Fairytale II - Cure a Queen", new VarTuple(0, VarbitID.FAIRY2_QUEENCURE_QUEST));
		questVarbs.put("Family Crest", new VarTuple(1, VarPlayerID.CRESTQUEST));
		questVarbs.put("The Feud", new VarTuple(0, VarbitID.FEUD_VAR));
		questVarbs.put("Fight Arena", new VarTuple(1, VarPlayerID.ARENAQUEST));
		questVarbs.put("Fishing Contest", new VarTuple(1, VarPlayerID.FISHINGCOMPO));
		questVarbs.put("Forgettable Tale...", new VarTuple(0, VarbitID.FORGET_QUEST));
		questVarbs.put("Bone Voyage", new VarTuple(0, VarbitID.FOSSILQUEST_PROGRESS));
		questVarbs.put("The Fremennik Isles", new VarTuple(0, VarbitID.FRIS_QUEST));
		questVarbs.put("The Fremennik Trials", new VarTuple(1, VarPlayerID.VIKING));
		questVarbs.put("Garden of Tranquillity", new VarTuple(0, VarbitID.GARDEN_QUEST));
		questVarbs.put("Gertrude's Cat", new VarTuple(1, VarPlayerID.FLUFFS));
		questVarbs.put("Ghosts Ahoy", new VarTuple(0, VarbitID.AHOY_QUESTVAR));
		questVarbs.put("The Giant Dwarf", new VarTuple(0, VarbitID.GIANTDWARF_QUEST));
		questVarbs.put("The Golem", new VarTuple(0, VarbitID.GOLEM_A));
		questVarbs.put("The Grand Tree", new VarTuple(1, VarPlayerID.GRANDTREE));
		questVarbs.put("The Great Brain Robbery", new VarTuple(1, VarPlayerID.BRAIN_QUEST_VAR));
		questVarbs.put("Grim Tales", new VarTuple(0, VarbitID.GRIM_QUEST));
		questVarbs.put("The Hand in the Sand", new VarTuple(0, VarbitID.HANDSAND_QUEST));
		questVarbs.put("Haunted Mine", new VarTuple(1, VarPlayerID.HAUNTEDMINE));
		questVarbs.put("Hazeel Cult", new VarTuple(1, VarPlayerID.HAZEELCULTQUEST));
		questVarbs.put("Heroes' Quest", new VarTuple(1, VarPlayerID.HEROQUEST));
		questVarbs.put("Holy Grail", new VarTuple(1, VarPlayerID.GRAIL));
		questVarbs.put("Horror from the Deep", new VarTuple(0, VarbitID.HORRORQUEST));
		questVarbs.put("Icthlarin's Little Helper", new VarTuple(0, VarbitID.ICS_LITTLE_VAR));
		questVarbs.put("In Aid of the Myreque", new VarTuple(0, VarbitID.MYREQUE_2_QUEST));
		questVarbs.put("In Search of the Myreque", new VarTuple(1, VarPlayerID.ROUTEQUEST));
		questVarbs.put("Jungle Potion", new VarTuple(1, VarPlayerID.JUNGLEPOTION));
		questVarbs.put("King's Ransom", new VarTuple(0, VarbitID.KR_QUEST));
		questVarbs.put("Legends' Quest", new VarTuple(1, VarPlayerID.LEGENDSQUEST));
		questVarbs.put("Lost City", new VarTuple(1, VarPlayerID.ZANARIS));
		questVarbs.put("The Lost Tribe", new VarTuple(0, VarbitID.LOST_TRIBE_QUEST));
		questVarbs.put("Lunar Diplomacy", new VarTuple(0, VarbitID.LUNAR_QUEST_MAIN));
		questVarbs.put("Making Friends with My Arm", new VarTuple(0, VarbitID.MY2ARM_STATUS));
		questVarbs.put("Making History", new VarTuple(0, VarbitID.MAKINGHISTORY_PROG));
		questVarbs.put("Merlin's Crystal", new VarTuple(1, VarPlayerID.ARTHUR));
		questVarbs.put("Monkey Madness I", new VarTuple(1, VarPlayerID.MM_MAIN));
		questVarbs.put("Monkey Madness II", new VarTuple(0, VarbitID.MM2_PROGRESS));
		questVarbs.put("Monk's Friend", new VarTuple(1, VarPlayerID.DRUNKMONKQUEST));
		questVarbs.put("Mountain Daughter", new VarTuple(0, VarbitID.MDAUGHTER_QUEST_VAR));
		questVarbs.put("Mourning's End Part I", new VarTuple(1, VarPlayerID.MOURNING_QUEST));
		questVarbs.put("Mourning's End Part II", new VarTuple(0, VarbitID.MOURNING_QUEST_MAIN));
		questVarbs.put("Murder Mystery", new VarTuple(1, VarPlayerID.MURDERQUEST));
		questVarbs.put("My Arm's Big Adventure", new VarTuple(0, VarbitID.MYARM));
		questVarbs.put("Nature Spirit", new VarTuple(1, VarPlayerID.DRUIDSPIRIT));
		questVarbs.put("Observatory Quest", new VarTuple(1, VarPlayerID.ITGRONIGEN));
		questVarbs.put("Olaf's Quest", new VarTuple(0, VarbitID.OLAF_QUEST_VAR));
		questVarbs.put("One Small Favour", new VarTuple(1, VarPlayerID.ONESMALLFAVOUR));
		questVarbs.put("Plague City", new VarTuple(1, VarPlayerID.ELENAQUEST));
		questVarbs.put("Priest in Peril", new VarTuple(1, VarPlayerID.PRIESTPERIL));
		questVarbs.put("The Queen of Thieves", new VarTuple(0, VarbitID.PISCQUEST));
		questVarbs.put("Rag and Bone Man I", new VarTuple(1, VarPlayerID.RAG_QUEST));
		questVarbs.put("Rag and Bone Man II", new VarTuple(1, VarPlayerID.RAG_BONE_2));
		questVarbs.put("Ratcatchers", new VarTuple(0, VarbitID.RATCATCH_VAR));
		questVarbs.put("Recipe for Disaster", new VarTuple(0, VarbitID.HUNDRED_MAIN_QUEST_VAR));
		questVarbs.put("Recruitment Drive", new VarTuple(0, VarbitID.RD_MAIN));
		questVarbs.put("Regicide", new VarTuple(1, VarPlayerID.REGICIDE_QUEST));
		questVarbs.put("Roving Elves", new VarTuple(1, VarPlayerID.ROVING_ELVES_QUEST));
		questVarbs.put("Royal Trouble", new VarTuple(0, VarbitID.ROYAL_QUEST));
		questVarbs.put("Rum Deal", new VarTuple(1, VarPlayerID.DEAL_QUEST));
		questVarbs.put("Scorpion Catcher", new VarTuple(1, VarPlayerID.SCORPCATCHER));
		questVarbs.put("Sea Slug", new VarTuple(1, VarPlayerID.SEASLUGQUEST));
		questVarbs.put("Shades of Mort'ton", new VarTuple(1, VarPlayerID.MORTTONQUEST));
		questVarbs.put("Shadow of the Storm", new VarTuple(0, VarbitID.AGRITH_QUEST));
		questVarbs.put("Sheep Herder", new VarTuple(1, VarPlayerID.SHEEPHERDERQUEST));
		questVarbs.put("Shilo Village", new VarTuple(1, VarPlayerID.ZOMBIEQUEEN));
		questVarbs.put("The Slug Menace", new VarTuple(0, VarbitID.SLUG2_MAIN));
		questVarbs.put("A Soul's Bane", new VarTuple(0, VarbitID.SOULBANE_PROG));
		questVarbs.put("Spirits of the Elid", new VarTuple(0, VarbitID.ELIDQUEST));
		questVarbs.put("Swan Song", new VarTuple(0, VarbitID.SWANSONG));
		questVarbs.put("Tai Bwo Wannai Trio", new VarTuple(1, VarPlayerID.TBWT_MAIN));
		questVarbs.put("A Tail of Two Cats", new VarTuple(0, VarbitID.TWOCATS_QUEST));
		questVarbs.put("Tale of the Righteous", new VarTuple(0, VarbitID.SHAYZIENQUEST));
		questVarbs.put("A Taste of Hope", new VarTuple(0, VarbitID.MYQ4));
		questVarbs.put("Tears of Guthix", new VarTuple(0, VarbitID.TOG_JUNA_BOWL));
		questVarbs.put("Temple of Ikov", new VarTuple(1, VarPlayerID.IKOV));
		questVarbs.put("Throne of Miscellania", new VarTuple(1, VarPlayerID.MISC_QUEST));
		questVarbs.put("The Tourist Trap", new VarTuple(1, VarPlayerID.DESERTRESCUE));
		questVarbs.put("Tower of Life", new VarTuple(0, VarbitID.TOL_PROG));
		questVarbs.put("Tree Gnome Village", new VarTuple(1, VarPlayerID.TREEQUEST));
		questVarbs.put("Tribal Totem", new VarTuple(1, VarPlayerID.TOTEMQUEST));
		questVarbs.put("Troll Romance", new VarTuple(1, VarPlayerID.TROLL_LOVE));
		questVarbs.put("Troll Stronghold", new VarTuple(1, VarPlayerID.TROLL_QUEST));
		questVarbs.put("Underground Pass", new VarTuple(1, VarPlayerID.UPASS));
		questVarbs.put("Client of Kourend", new VarTuple(0, VarbitID.VEOS_PROGRESS));
		questVarbs.put("Wanted!", new VarTuple(0, VarbitID.WANTED_MAIN));
		questVarbs.put("Watchtower", new VarTuple(1, VarPlayerID.ITWATCHTOWER));
		questVarbs.put("Waterfall Quest", new VarTuple(1, VarPlayerID.WATERFALL_QUEST));
		questVarbs.put("What Lies Below", new VarTuple(0, VarbitID.SUROK_QUEST));
		questVarbs.put("Witch's House", new VarTuple(1, VarPlayerID.BALLQUEST));
		questVarbs.put("Zogre Flesh Eaters", new VarTuple(0, VarbitID.ZOGRE));
		questVarbs.put("The Ascent of Arceuus", new VarTuple(0, VarbitID.ARCQUEST));
		questVarbs.put("The Forsaken Tower", new VarTuple(0, VarbitID.LOVAQUEST));
		questVarbs.put("Song of the Elves", new VarTuple(0, VarbitID.SOTE));
		questVarbs.put("The Fremennik Exiles", new VarTuple(0, VarbitID.VIKINGEXILE));
		questVarbs.put("Sins of the Father", new VarTuple(0, VarbitID.MYQ5));
		questVarbs.put("A Porcine of Interest", new VarTuple(0, VarbitID.PORCINE));
		questVarbs.put("Getting Ahead", new VarTuple(0, VarbitID.GA));
		questVarbs.put("A Kingdom Divided", new VarTuple(0, VarbitID.AKD));
		questVarbs.put("A Night at the Theatre", new VarTuple(0, VarbitID.TOBQUEST));
	}

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event)
	{
		// Only check if the diary/quest widget text is changing
		if (event.getGroupId() != InterfaceID.QUESTJOURNAL)
			return;
		Widget w = client.getWidget(InterfaceID.Questjournal.TEXTLAYER);
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
		Widget titleWidget = client.getWidget(InterfaceID.Questjournal.TITLE);
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
			QuestLogData data = new QuestLogData(key, false, VarPlayerID.PHOENIXGANG, client.getVarpValue(VarPlayerID.PHOENIXGANG), s.toString().trim());
			QuestLogData data2 = new QuestLogData(key, false, VarPlayerID.BLACKARMGANG, client.getVarpValue(VarPlayerID.BLACKARMGANG), s.toString().trim());
			manager.storeEvent(data);
			manager.storeEvent(data2);
		}
		else {
			log.debug("No match! " + key);
		}
	}

}
