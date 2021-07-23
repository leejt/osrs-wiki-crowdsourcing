package com.Crowdsourcing.animation;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.HashSet;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingAnimation
{
	@Inject
	public CrowdsourcingManager manager;

	@Inject
	public Client client;

	@Inject
	public ClientThread clientThread;

	// Cache seen anims so we avoid sending them more than once
	private static HashSet<Integer> seenPlayerAnims = new HashSet<>();
	private static HashSet<Integer> seenNpcAnims = new HashSet<>();


	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() instanceof Player)
		{
			Player p = (Player) event.getActor();
			if (seenPlayerAnims.contains(p.getAnimation()))
			{
				return;
			}
			seenPlayerAnims.add(p.getAnimation());
			clientThread.invokeLater(() ->
			{
				// Grab the player's location
				LocalPoint local = LocalPoint.fromWorld(client, p.getWorldLocation());
				if (local == null)
				{
					return;
				}
				WorldPoint location = WorldPoint.fromLocalInstance(client, local);
				boolean isInInstance = client.isInInstancedRegion();

				// Create and send the animation data
				AnimationData data = new AnimationData(p.getAnimation(), -1, true, isInInstance, location);
				log.trace("Player anim id: {} stored", p.getAnimation());
				manager.storeEvent(data);
			});
		}
		else if (event.getActor() instanceof NPC)
		{
			NPC seenNpc = (NPC) event.getActor();
			NPCComposition seenNpcComposition = seenNpc.getTransformedComposition();
			if (seenNpcComposition == null || seenNpcComposition.getId() == -1 || seenNpc.getAnimation() == -1)
			{
				return;
			}

			// Calculate a key for the blacklist
			int key = (seenNpcComposition.getId() << 16) + seenNpc.getAnimation();
			if (seenNpcAnims.contains(key))
			{
				return;
			}
			seenNpcAnims.add(key);
			clientThread.invokeLater(() ->
			{
				// Grab the NPC's location
				LocalPoint local = LocalPoint.fromWorld(client, seenNpc.getWorldLocation());
				if (local == null)
				{
					return;
				}
				WorldPoint location = WorldPoint.fromLocalInstance(client, local);
				boolean isInInstance = client.isInInstancedRegion();

				// Create and send the animation data
				AnimationData data = new AnimationData(seenNpc.getAnimation(), seenNpcComposition.getId(), false, isInInstance, location);
				log.trace("NPC id {}: anim id {} stored", seenNpcComposition.getId(), seenNpc.getAnimation());
				manager.storeEvent(data);
			});
		}

	}
}
