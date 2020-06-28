package com.Crowdsourcing.thieving;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;

public class CrowdsourcingThieving
{

	private static final String BLACKJACK_SUCCESS = "You smack the bandit over the head and render them unconscious.";
	private static final String BLACKJACK_FAIL = "Your blow only glances off the bandit's head.";
	private static final Pattern PICKPOCKET_SUCCESS = Pattern.compile("You pick .*'s pocket\\.");
	private static final Pattern PICKPOCKET_FAIL = Pattern.compile("You fail to pick .*'s pocket\\.");

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	private int lastPickpocketTarget;

	private boolean hasGlovesOfSilence()
	{
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipmentContainer == null)
		{
			return false;
		}

		Item[] items = equipmentContainer.getItems();
		int idx = EquipmentInventorySlot.GLOVES.getSlotIdx();

		if (items == null || idx >= items.length)
		{
			return false;
		}

		Item glove = items[idx];
		return glove != null && glove.getId() == ItemID.GLOVES_OF_SILENCE;
	}

	private boolean hasThievingCape()
	{
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipmentContainer == null)
		{
			return false;
		}

		Item[] items = equipmentContainer.getItems();
		int idx = EquipmentInventorySlot.CAPE.getSlotIdx();

		if (items == null || idx >= items.length)
		{
			return false;
		}

		Item cape = items[idx];
		if (cape == null)
		{
			return false;
		}

		int capeId = cape.getId();
		return capeId == ItemID.THIEVING_CAPE || capeId == ItemID.THIEVING_CAPET || capeId == ItemID.MAX_CAPE;
	}

	private int getArdougneDiary()
	{
		int easy = client.getVar(Varbits.DIARY_ARDOUGNE_EASY);
		int medium = client.getVar(Varbits.DIARY_ARDOUGNE_MEDIUM);
		int hard = client.getVar(Varbits.DIARY_ARDOUGNE_HARD);
		int elite = client.getVar(Varbits.DIARY_ARDOUGNE_ELITE);
		return easy + 2 * medium + 4 * hard + 8 * elite;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		String message = event.getMessage();
		if (BLACKJACK_SUCCESS.equals(message)
			|| BLACKJACK_FAIL.equals(message)
			|| PICKPOCKET_FAIL.matcher(message).matches()
			|| PICKPOCKET_SUCCESS.matcher(message).matches())
		{
			WorldPoint location = client.getLocalPlayer().getWorldLocation();
			int ardougneDiary = getArdougneDiary();
			boolean silence = hasGlovesOfSilence();
			boolean thievingCape = hasThievingCape();
			PickpocketData data = new PickpocketData(lastPickpocketTarget, message, location, silence, thievingCape, ardougneDiary);
			manager.storeEvent(data);
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (event.getMenuOption().equals("Pickpocket") || event.getMenuOption().equals("Knock-Out"))
		{
			lastPickpocketTarget = event.getId();
		}
	}
}