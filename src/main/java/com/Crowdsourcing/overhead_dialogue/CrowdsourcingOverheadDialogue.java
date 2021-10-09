package com.Crowdsourcing.overhead_dialogue;

import com.Crowdsourcing.CrowdsourcingManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.client.eventbus.Subscribe;

import java.util.concurrent.ExecutionException;

@Slf4j
public class CrowdsourcingOverheadDialogue
{
	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;


	public static CacheLoader<Tuple, Boolean> loader;
	public static LoadingCache<Tuple, Boolean> recentlySeen;

	static
	{
		loader = new CacheLoader<Tuple, Boolean>()
		{
			@Override
			public Boolean load(Tuple key)
			{
				return true;
			}
		};
		recentlySeen = CacheBuilder.newBuilder().maximumSize(100).build(loader);
	}

	static class Tuple
	{
		public final int npcId;
		public final String text;

		public Tuple(int npcId, String text)
		{
			this.npcId = npcId;
			this.text = text;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof Tuple))
			{
				return false;
			}
			Tuple t = (Tuple) o;
			return ((this.npcId == t.npcId) && (this.text.equals(t.text)));
		}

		@Override
		public int hashCode() {
			int hash = 17;
			hash = hash * 486187739 + this.npcId;
			hash = hash * 486187739 + this.text.hashCode();
			return hash;
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
		if (recentlySeen.getIfPresent(npcPair) != null)
		{
			log.debug("Already saw this pair in the last 100 sightings");
			return;
		}
		// Note that we actually get dialogue for all variants of an NPC, even if they are not visible due to varbits.
		// Grabbing the name() of these yields null while they are invisible, so just use the NPC id.
		OverheadDialogueData data = new OverheadDialogueData(npc.getComposition().getId(), event.getOverheadText());

		manager.storeEvent(data);
		try
		{
			log.debug("Message recorded from NPC id: " + npc.getComposition().getId());
			recentlySeen.get(npcPair);
		}
		catch (ExecutionException e)
		{
			log.debug("ExecutionException caught");
		}
	}

}
