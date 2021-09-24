package com.Crowdsourcing.overhead_dialogue;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingOverheadDialogue
{
	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		// Ignore non-NPC overhead dialogue
		if (!(event.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) event.getActor();
		// Note that we actually get dialogue for all variants of an NPC, even if they are not visible due to varbits.
		// Grabbign the name() of these yields null while they are invisible, so just use the NPC id.
		OverheadDialogueData data = new OverheadDialogueData(npc.getComposition().getId(), event.getOverheadText());

		manager.storeEvent(data);

	}

}
