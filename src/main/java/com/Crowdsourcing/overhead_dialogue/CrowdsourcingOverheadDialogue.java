package com.Crowdsourcing.overhead_dialogue;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.collect.EvictingQueue;
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

	EvictingQueue recentlySeen = EvictingQueue.<Tuple>create(30);

	static class Tuple {
		public final int npcId;
		public final String text;
		public Tuple(int npcId, String text) {
			this.npcId = npcId;
			this.text = text;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof Tuple))
				return false;
			Tuple t = (Tuple) o;
			return ((this.npcId == t.npcId) && (this.text.equals(t.text)));
		}
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		// Ignore non-NPC overhead dialogue
		if (!(event.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) event.getActor();
		Tuple npcPair = new Tuple(npc.getComposition().getId(), event.getOverheadText());
		// If we have seen this npc, text pair recently, do not send it.
		if (recentlySeen.contains(npcPair))
		{
			log.debug("Already saw this pair in the last 30 sightings");
			return;
		}
		// Note that we actually get dialogue for all variants of an NPC, even if they are not visible due to varbits.
		// Grabbing the name() of these yields null while they are invisible, so just use the NPC id.
		OverheadDialogueData data = new OverheadDialogueData(npc.getComposition().getId(), event.getOverheadText());

		manager.storeEvent(data);
		recentlySeen.add(npcPair);
	}

}
