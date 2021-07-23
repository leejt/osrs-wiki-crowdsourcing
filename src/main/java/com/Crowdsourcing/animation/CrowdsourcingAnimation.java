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
			if (p == null || seenPlayerAnims.contains(p.getAnimation()))
			{
				return;
			}
			seenPlayerAnims.add(p.getAnimation());
			clientThread.invokeLater(() ->
			{
				LocalPoint local = LocalPoint.fromWorld(client, p.getWorldLocation());
				if (local == null)
				{
					return;
				}
				WorldPoint location = WorldPoint.fromLocalInstance(client, local);
				boolean isInInstance = client.isInInstancedRegion();
				AnimationData data = new AnimationData(p.getAnimation(), -1, true, isInInstance, location);
				log.trace("Player anim id: {} stored", p.getAnimation());
				manager.storeEvent(data);
			});
		}
		else if (event.getActor() instanceof NPC)
		{
			NPC n = (NPC) event.getActor();
			NPCComposition nc = n.getTransformedComposition();
			if (nc == null)
			{
				return;
			}
			if (nc.getId() == -1 || n.getAnimation() == -1 || seenNpcAnims.contains((nc.getId() << 16) + n.getAnimation()))
			{
				return;
			}
			n.getWorldLocation();
			seenNpcAnims.add((nc.getId() << 16) + n.getAnimation());
			clientThread.invokeLater(() ->
			{
				LocalPoint local = LocalPoint.fromWorld(client, n.getWorldLocation());
				if (local == null)
				{
					return;
				}
				WorldPoint location = WorldPoint.fromLocalInstance(client, local);
				boolean isInInstance = client.isInInstancedRegion();
				AnimationData data = new AnimationData(n.getAnimation(), nc.getId(), false, isInInstance, location);
				log.trace("NPC id {}: anim id {} stored", nc.getId(), n.getAnimation());
				manager.storeEvent(data);
			});
		}

	}
}
