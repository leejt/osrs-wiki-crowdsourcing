package com.Crowdsourcing;

import com.Crowdsourcing.animation.CrowdsourcingAnimation;
import com.Crowdsourcing.clues.CrowdsourcingClues;
import com.Crowdsourcing.dialogue.CrowdsourcingDialogue;
import com.Crowdsourcing.experience.CrowdsourcingExperience;
import com.Crowdsourcing.inventory.CrowdsourcingInventory;
import com.Crowdsourcing.item_sighting.CrowdsourcingItemSighting;
import com.Crowdsourcing.messages.CrowdsourcingMessages;
import com.Crowdsourcing.mlm.CrowdsourcingMLM;
import com.Crowdsourcing.monster_examine.MonsterExamine;
import com.Crowdsourcing.npc_sighting.CrowdsourcingNpcSighting;
import com.Crowdsourcing.npc_respawn.CrowdsourcingNpcRespawn;
import com.Crowdsourcing.overhead_dialogue.CrowdsourcingOverheadDialogue;
import com.Crowdsourcing.playerkit.CrowdsourcingPlayerkit;
import com.Crowdsourcing.pottery.CrowdsourcingPottery;
import com.Crowdsourcing.pyramid_plunder.CrowdsourcingPyramidPlunder;
import com.Crowdsourcing.quest_log.CrowdsourcingQuestLog;
import com.Crowdsourcing.respawns.Respawns;
import com.Crowdsourcing.scenery.CrowdsourcingScenery;
import com.Crowdsourcing.toa.CrowdsourcingTombs;
import com.Crowdsourcing.varbits.CrowdsourcingVarbits;
import javax.inject.Inject;
import java.time.temporal.ChronoUnit;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.events.CommandExecuted;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

@Slf4j
@PluginDescriptor(
	name = "OSRS Wiki Crowdsourcing (advanced)",
	description = "Help figure out varbits, quest states, and more. See osrs.wiki/RS:CROWD"
)
public class AdvancedCrowdsourcingPlugin extends Plugin
{
	// Number of seconds to wait between trying to send data to the wiki.
	// NOTE: I wanted to make this a config entry but annotation parameters
	// need to be compile time constants.
	private static final int SECONDS_BETWEEN_UPLOADS = 300;

	@Inject
	private EventBus eventBus;

	@Inject
	CrowdsourcingManager manager;

	@Inject
	private CrowdsourcingDialogue dialogue;

	@Inject
	private CrowdsourcingVarbits varbits;

	@Inject
	private CrowdsourcingMLM mlm;

	@Inject
	private CrowdsourcingScenery scenery;

	@Inject
	private CrowdsourcingMessages messages;

	@Inject
	private CrowdsourcingPlayerkit playerkit;

	@Inject
	private CrowdsourcingNpcSighting npcSighting;

	@Inject
	private CrowdsourcingItemSighting itemSighting;

	@Inject
	private CrowdsourcingInventory inventory;

	@Inject
	private CrowdsourcingClues clues;

	@Inject
	private CrowdsourcingAnimation animation;

	@Inject
	private CrowdsourcingNpcRespawn npcRespawn;

	@Inject
	private Respawns respawns;

	@Inject
	private MonsterExamine monsterExamine;

	@Inject
	private CrowdsourcingQuestLog questLog;

	@Inject
	private CrowdsourcingOverheadDialogue overheadDialogue;

	@Inject
	private CrowdsourcingPottery pottery;

	@Inject
	private CrowdsourcingExperience experience;

  @Inject
	private CrowdsourcingPyramidPlunder pyramidPlunder;

	@Inject
	private CrowdsourcingTombs toa;

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(dialogue);
		eventBus.register(varbits);
		eventBus.register(mlm);
		eventBus.register(scenery);
		eventBus.register(messages);
		eventBus.register(playerkit);
		eventBus.register(npcSighting);
		eventBus.register(itemSighting);
		eventBus.register(inventory);
		eventBus.register(clues);
		eventBus.register(animation);
		eventBus.register(npcRespawn);
		eventBus.register(questLog);
		eventBus.register(overheadDialogue);
		eventBus.register(pottery);
		eventBus.register(experience);
		eventBus.register(pyramidPlunder);
		eventBus.register(toa);

		varbits.startUp();
		experience.startUp();
		pyramidPlunder.startUp();
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(dialogue);
		eventBus.unregister(varbits);
		eventBus.unregister(mlm);
		eventBus.unregister(scenery);
		eventBus.unregister(messages);
		eventBus.unregister(playerkit);
		eventBus.unregister(npcSighting);
		eventBus.unregister(itemSighting);
		eventBus.unregister(inventory);
		eventBus.unregister(clues);
		eventBus.unregister(animation);
		eventBus.unregister(npcRespawn);
		eventBus.unregister(questLog);
		eventBus.unregister(overheadDialogue);
		eventBus.unregister(pottery);
		eventBus.unregister(experience);
		eventBus.unregister(pyramidPlunder);
		eventBus.unregister(toa);

		varbits.shutDown();
	}

	@Schedule(
		period = SECONDS_BETWEEN_UPLOADS,
		unit = ChronoUnit.SECONDS,
		asynchronous = true
	)
	public void submitToAPI()
	{
		manager.submitToAPI();
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		String cmd = commandExecuted.getCommand();
		switch (cmd) {
			case "respawnon":
				eventBus.register(respawns);
				manager.sendMessage("Turned on respawns logger.");
				npcRespawn.setLogging(true);
				break;
			case "respawnoff":
				eventBus.unregister(respawns);
				manager.sendMessage("Turned off respawns logger.");
				npcRespawn.setLogging(false);
				break;
			case "monsteron":
				eventBus.register(monsterExamine);
				monsterExamine.startUp();
				manager.sendMessage("Turned on monster examine logger.");
				break;
			case "monsteroff":
				eventBus.unregister(monsterExamine);
				monsterExamine.shutDown();
				manager.sendMessage("Turned off monster examine logger.");
				break;
		}
	}
}
