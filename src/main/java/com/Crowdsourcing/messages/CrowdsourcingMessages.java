package com.Crowdsourcing.messages;

import com.Crowdsourcing.CrowdsourcingManager;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Skill;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingMessages
{
	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	// Make sure anything here ONLY matches what you want
	private static final String CAIRN_ISLE_SUCCESS = "You manage to keep your balance on the bridge.";
	private static final String CAIRN_ISLE_FAIL = "You fall!";

	// Ardy log shortcut, log NW of sinclair mansion, Karamja east log
	private static final String LOG_SUCCESS = "You make it across the log without any problems.";
	private static final String LOG_FAIL = "You lose your footing and fall into the water.";

	// Rock jumps in lumbridge caves
	private static final String STEPPING_STONE_ATTEMPT = "You leap across with a mighty leap!";
	private static final String STEPPING_STONE_FAIL = "You slip over on the slimy stone.";

	// Lava dragon scales, store herblore level and hard diary completion
	private static final int HARD_WILDERNESS_DIARY_VARBIT = 4509;
	private static final String LAVA_DRAGON_SCALE_GRIND_PREFIX = "You grind the lava dragon scale";

	// Sacred eel -> Zulrah scales, cooking level
	private static final String SACRED_EEL_DISSECTION_PREFIX = "You dissect the eel carcass";

	// Undead twigs, presumably add WC level
	private static final String UNDEAD_TWIGS_SUCCESS = "You cut some undead twigs.";
	private static final String UNDEAD_TWIGS_FAIL = "You almost remove a suitable twig, but you don't quite manage it.";

	// Wire machine, presumably add Thieving level
	private static final String WIRE_MACHINE_SUCCESS = "You grab a piece of wire.";
	private static final String WIRE_MACHINE_FAIL = "You catch your hand in the mechanism.";

	// Vyre distraction
	private static final String VYRE_DISTRACTION_SUCCESS = "You manage to distract the vampyre and sneak away.";
	private static final String VYRE_DISTRACTION_FAIL = "You failed to distract the vampyre.";

	// Zogre coffins (need to do some more checking here, lockpick might complicate things and need to check message uniqueness)
	private static final String ZOGRE_COFFIN_SUCCESS = "You unlock the coffin...";
	private static final String ZOGRE_COFFIN_FAIL = "You fail to pick the lock - your fingers get numb from fumbling with the lock.";
	private static final String ZOGRE_COFFIN_LOCKPICK_SNAPS = "Your lockpick snaps.";

	// Pickable doors (Yanille dungeon and Ross' door for example)
	private static final String PICKLOCK_DOOR_SUCCESS = "You manage to pick the lock.";
	private static final String PICKLOCK_DOOR_FAIL = "You fail to pick the lock.";
	private static final String PICKLOCK_DOOR_TRAP = "You have activated a trap on the lock.";

	// Pickable chests (Aldarin for example)
	private static final String PICKLOCK_CHEST_SUCCESS = "You manage to unlock the chest.";
	private static final String PICKLOCK_CHEST_FAIL = "You fail to picklock the chest.";
	private static final String PICKLOCK_CHEST_TRAP = "You have activated a trap on the chest.";

	// Viyeldi Caves rock mining
	private static final String VIYELDI_ROCK_MINING_SUCCESS = "You manage to smash the rock to bits.";
	private static final String VIYELDI_ROCK_MINING_FAIL = "The pick clangs heavily against the rock face and the vibrations rattle your nerves.";

	// Viyeldi Caves jagged wall
	private static final String VIYELDI_JAGGED_WALL_SUCCESS = "You take a good run up and sail majestically over the wall.";
	private static final String VIYELDI_JAGGED_WALL_FAIL = "You fail to jump the wall properly and clip the wall with your leg.";

	// Stealing entrana candles
	private static final String ENTRANA_CANDLE_SUCCESS = "You steal a candle.";
	private static final String ENTRANA_CANDLE_FAIL = "A higher power smites you.";

	// Underground Pass things
	private static final String UP_ROCKSLIDE_SUCCESS = "You climb over the rocks.";
	private static final String UP_ROCKSLIDE_FAIL = "You slip and hurt yourself.";

	private static final String UP_FIREARROW_SUCCESS = "...the arrow impales the rope support.";
	private static final String UP_FIREARROW_FAIL = "...but the arrow just misses the rope.";

	private static final String UP_ROPESWING_SUCCESS = "...and skillfully swing across.";
	private static final String UP_ROPESWING_FAIL = "...but you fall into the darkness.";

	private static final String UP_MARKINGS_TRAP_SUCCESS = "...and succeed, you quickly walk past.";
	private static final String UP_MARKINGS_TRAP_FAIL = "... and fail, activating the trap!";

	private static final String UP_SLAVE_CAGE_LOCKPICK_SUCCESS = "You successfully pick the lock.";
	private static final String UP_SLAVE_CAGE_LOCKPICK_FAIL = "You attempt to pick the lock but fail to do so.";

	private static final String UP_LEDGE_WALK_SUCCESS = "...you make it across safely.";
	private static final String UP_LEDGE_WALK_FAIL = "...but you fall into the rat pit.";

	private static final String UP_STONE_BRIDGE_SUCCESS = "...and make it.";
	private static final String UP_STONE_BRIDGE_FAIL = "...and fall off it.";

	private static final String UP_BRIDGE_SUCCESS = "...you manage to cross safely.";
	private static final String UP_BRIDGE_FAIL = "...but you tumble into the darkness.";

	private static final String CACTUS_SUCCESS_1 = "You top up your skin with water from the cactus.";
	private static final String CACTUS_SUCCESS_2 = "You fill your skin with water from the cactus.";
	private static final String CACTUS_SUCCESS_3 = "You have no empty waterskins to put the water in.";
	private static final String CACTUS_FAIL = "You fail to cut the cactus correctly and it gives no water this time.";

	// This message gives us number of successes without a fragment
	private static final String VM_BOULDER_SUCCESS = "You chip off a part of the boulder.";
	private static final String VM_BOULDER_REWARDS_PREFIX = "You mine out ";

	// Farming pickpockets
	private static final Pattern MASTER_FARMER_PICKPOCKET = Pattern.compile("You steal 1 " +
		"(cadantine|dwarf weed|guam|harralander|irit|kwuarm|lantadyme|marrentill|ranarr|snapdragon|tarromin|toadflax|torstol) seed.*");

	// Control
	private static final String TUNA_SUCCESS = "You manage to cook a tuna.";
	private static final String TUNA_FAIL = "You accidentally burn the tuna.";

	// Pet fish tiny bluefish, greenfish, spinefish
	private static final String PET_FISH_BLUEFISH = "...and you catch a Tiny Bluefish!";
	private static final String PET_FISH_GREENFISH = "...and you catch a Tiny Greenfish!";
	private static final String PET_FISH_SPINEFISH = "...and you catch a Tiny Spinefish!";

	// Camdozaal preparing fish
	private static final String CAMDOZAAL_PREPARE_GUPPY_SUCCESS = "You successfully prepare the Guppy.";
	private static final String CAMDOZAAL_PREPARE_CAVEFISH_SUCCESS = "You successfully prepare the Cavefish.";
	private static final String CAMDOZAAL_PREPARE_TETRA_SUCCESS = "You successfully prepare the Tetra.";
	private static final String CAMDOZAAL_PREPARE_CATFISH_SUCCESS = "You successfully prepare the Catfish.";
	private static final String CAMDOZAAL_PREPARE_GUPPY_FAIL = "You accidentally ruin the Guppy.";
	private static final String CAMDOZAAL_PREPARE_CAVEFISH_FAIL = "You accidentally ruin the Cavefish.";
	private static final String CAMDOZAAL_PREPARE_TETRA_FAIL = "You accidentally ruin the Tetra.";
	private static final String CAMDOZAAL_PREPARE_CATFISH_FAIL = "You accidentally ruin the Catfish.";
	private static final int CHARM_OF_PREPARATION = 12100;
	private static final int CHARM_OF_BOOST = 12104;

	// Temple of Marimbo Dungeon entrance via crate during Monkey Madness 1
	private static final String CRATE_4714_CATLIKE_AGILITY_ATTEMPT = "You begin to lower yourself into the hole...";
	private static final String CRATE_4714_CATLIKE_AGILITY_SUCCESS = "... and using your catlike agility land on all fours at the bottom of a large cavern!";

  // Hallowed Sepulchre coffins
  private static final String SEPULCHRE_FAILURE = "You have been poisoned!";
  private static final String SEPULCHRE_FAILURE_ANTIPOISON = "You trigger a trap on the chest which poisons you!";
  private static final String SEPULCHRE_SUCCESS = "You push the coffin lid aside.";

	private HashMap<String, Object> createSkillMap(Skill s)
	{
		HashMap<String, Object> h = new HashMap<>();
		h.put(s.getName(), client.getRealSkillLevel(s));
		h.put("B" + s.getName(), client.getBoostedSkillLevel(s));
		return h;
	}

	public HashMap<String, Object> getMetadataForMessage(String message)
	{
		// For each message, check if we need to add metadata. If so, add it to the hashmap to be returned.

		// Should these just be a bunch of ImmutableSets and checks on contains?
		if (CAIRN_ISLE_SUCCESS.equals(message) || CAIRN_ISLE_FAIL.equals(message))
		{
			return createSkillMap(Skill.AGILITY);
		}

		if (LOG_SUCCESS.equals(message) || LOG_FAIL.equals(message))
		{
			return createSkillMap(Skill.AGILITY);
		}

		if (STEPPING_STONE_ATTEMPT.equals(message) || STEPPING_STONE_FAIL.equals(message))
		{
			return createSkillMap(Skill.AGILITY);
		}

		if (message.startsWith(LAVA_DRAGON_SCALE_GRIND_PREFIX))
		{
			HashMap<String, Object> h = createSkillMap(Skill.HERBLORE);
			h.put("Diarycomplete", client.getVarbitValue(HARD_WILDERNESS_DIARY_VARBIT));
			return h;
		}

		if (message.startsWith(SACRED_EEL_DISSECTION_PREFIX))
		{
			return createSkillMap(Skill.COOKING);
		}

		if (UNDEAD_TWIGS_SUCCESS.equals(message) || UNDEAD_TWIGS_FAIL.equals(message)
			|| CACTUS_SUCCESS_1.equals(message) || CACTUS_SUCCESS_2.equals(message) || CACTUS_SUCCESS_3.equals(message)
			|| CACTUS_FAIL.equals(message))
		{
			return createSkillMap(Skill.WOODCUTTING);
		}

		if (WIRE_MACHINE_SUCCESS.equals(message) || WIRE_MACHINE_FAIL.equals(message))
		{
			return createSkillMap(Skill.THIEVING);
		}

		if (VYRE_DISTRACTION_SUCCESS.equals(message) || VYRE_DISTRACTION_FAIL.equals(message))
		{
			return createSkillMap(Skill.THIEVING);
		}

		if (ZOGRE_COFFIN_SUCCESS.equals(message)
			|| ZOGRE_COFFIN_FAIL.equals(message)
			|| ZOGRE_COFFIN_LOCKPICK_SNAPS.equals(message)
			|| PICKLOCK_DOOR_SUCCESS.equals(message)
			|| PICKLOCK_DOOR_FAIL.equals(message)
			|| PICKLOCK_DOOR_TRAP.equals(message)
			|| PICKLOCK_CHEST_SUCCESS.equals(message)
			|| PICKLOCK_CHEST_FAIL.equals(message)
			|| PICKLOCK_CHEST_TRAP.equals(message))
		{
			boolean hasLockpick = false;
			boolean hasHairClip = false;
			ItemContainer equipContainer = client.getItemContainer(InventoryID.INVENTORY);
			if (equipContainer != null)
			{
				final Item[] items = equipContainer.getItems();
				for (Item item : items)
				{
					if (item.getId() == ItemID.LOCKPICK)
						hasLockpick = true;
					else if (item.getId() == ItemID.HAIR_CLIP)
						hasHairClip = true;
				}
			}
			HashMap<String, Object> h = createSkillMap(Skill.THIEVING);
			h.put("Lockpick", hasLockpick);
			h.put("Hairclip", hasHairClip);
			return h;
		}

		if (VIYELDI_ROCK_MINING_SUCCESS.equals(message) || VIYELDI_ROCK_MINING_FAIL.equals(message)
			|| VM_BOULDER_SUCCESS.equals(message) || message.startsWith(VM_BOULDER_REWARDS_PREFIX))
		{
			return createSkillMap(Skill.MINING);
		}

		if (VIYELDI_JAGGED_WALL_SUCCESS.equals(message) || VIYELDI_JAGGED_WALL_FAIL.equals(message))
		{
			return createSkillMap(Skill.AGILITY);
		}

		if (ENTRANA_CANDLE_SUCCESS.equals(message) || ENTRANA_CANDLE_FAIL.equals(message))
		{
			return createSkillMap(Skill.THIEVING);
		}

		if (UP_ROCKSLIDE_SUCCESS.equals(message) || UP_ROCKSLIDE_FAIL.equals(message)
			|| UP_ROPESWING_SUCCESS.equals(message) || UP_ROPESWING_FAIL.equals(message)
			|| UP_LEDGE_WALK_SUCCESS.equals(message) || UP_LEDGE_WALK_FAIL.equals(message)
			|| UP_STONE_BRIDGE_SUCCESS.equals(message) || UP_STONE_BRIDGE_FAIL.equals(message)
			|| UP_BRIDGE_SUCCESS.equals(message) || UP_BRIDGE_FAIL.equals(message))
		{
			return createSkillMap(Skill.AGILITY);
		}

		if (UP_FIREARROW_SUCCESS.equals(message) || UP_FIREARROW_FAIL.equals(message))
		{
			return createSkillMap(Skill.RANGED);
		}

		if (UP_SLAVE_CAGE_LOCKPICK_SUCCESS.equals(message) || UP_SLAVE_CAGE_LOCKPICK_FAIL.equals(message)
			|| UP_MARKINGS_TRAP_SUCCESS.equals(message) || UP_MARKINGS_TRAP_FAIL.equals(message))
		{
			return createSkillMap(Skill.THIEVING);
		}

		if (TUNA_SUCCESS.equals(message) || TUNA_FAIL.equals(message))
		{
			return createSkillMap(Skill.COOKING);
		}

		if (MASTER_FARMER_PICKPOCKET.matcher(message).matches())
		{
			return createSkillMap(Skill.FARMING);
		}

		if (PET_FISH_BLUEFISH.equals(message) || PET_FISH_GREENFISH.equals(message) || PET_FISH_SPINEFISH.equals(message))
		{
			return createSkillMap(Skill.FISHING);
		}

		if (CAMDOZAAL_PREPARE_GUPPY_SUCCESS.equals(message)
			|| CAMDOZAAL_PREPARE_CAVEFISH_SUCCESS.equals(message)
			|| CAMDOZAAL_PREPARE_TETRA_SUCCESS.equals(message)
			|| CAMDOZAAL_PREPARE_CATFISH_SUCCESS.equals(message)
			|| CAMDOZAAL_PREPARE_GUPPY_FAIL.equals(message)
			|| CAMDOZAAL_PREPARE_CAVEFISH_FAIL.equals(message)
			|| CAMDOZAAL_PREPARE_TETRA_FAIL.equals(message)
			|| CAMDOZAAL_PREPARE_CATFISH_FAIL.equals(message))
		{
			HashMap<String, Object> h = createSkillMap(Skill.COOKING);
			h.put("CharmOfPreparation", client.getVarbitValue(CHARM_OF_PREPARATION) > 0);
			h.put("CharmOfBoost", client.getVarbitValue(CHARM_OF_BOOST) > 0);
			return h;
		}

		if (CRATE_4714_CATLIKE_AGILITY_ATTEMPT.equals(message)
			|| CRATE_4714_CATLIKE_AGILITY_SUCCESS.equals(message))
		{
			return createSkillMap(Skill.AGILITY);
		}

    if (SEPULCHRE_FAILURE.equals(message) || SEPULCHRE_SUCCESS.equals(message) || SEPULCHRE_FAILURE_ANTIPOISON.equals(message))
    {
      boolean hasLockpick = false;
      boolean hasStrangeOldLockpick = false;
      ItemContainer equipContainer = client.getItemContainer(InventoryID.INVENTORY);
      if (equipContainer != null)
      {
        final Item[] items = equipContainer.getItems();
        for (Item item : items)
        {
          if (item.getId() == ItemID.LOCKPICK)
            hasLockpick = true;
          if (item.getId() == ItemID.STRANGE_OLD_LOCKPICK || item.getId() == ItemID.STRANGE_OLD_LOCKPICK_FULL)
            hasStrangeOldLockpick = true;
        }
        HashMap<String, Object> h = createSkillMap(Skill.THIEVING);
        h.put("Lockpick", hasLockpick);
        h.put("StrangeLockpick", hasStrangeOldLockpick);
        return h;
      }
    }

		return null;
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE && chatMessage.getType() != ChatMessageType.SPAM && chatMessage.getType() != ChatMessageType.MESBOX)
		{
			return;
		}
		String message = chatMessage.getMessage();
		if (client == null || client.getLocalPlayer() == null)
		{
			return;
		}
		LocalPoint local = LocalPoint.fromWorld(client, client.getLocalPlayer().getWorldLocation());
		if (local == null)
		{
			return;
		}
		WorldPoint location = WorldPoint.fromLocalInstance(client, local);
		boolean isInInstance = client.isInInstancedRegion();
		HashMap<String, Object> metadata = getMetadataForMessage(message);
		MessagesData data = new MessagesData(message, isInInstance, location, metadata);
		// log.debug("" + data);
		manager.storeEvent(data);
	}
}
