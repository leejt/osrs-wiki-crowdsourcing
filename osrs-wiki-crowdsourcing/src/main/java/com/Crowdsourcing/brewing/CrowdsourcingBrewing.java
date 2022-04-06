/*
 * Copyright (c) 2019, Weird Gloop <admin@weirdgloop.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.Crowdsourcing.brewing;

import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.Varbits;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.crowdsourcing.CrowdsourcingManager;

public class CrowdsourcingBrewing
{
	private static final int KELDAGRIM_THE_STUFF_VARBIT = 2294;
	private static final int PORT_PHASMATYS_THE_STUFF_VARBIT = 2295;
	private boolean theStuffPortPhasmatys;
	private boolean theStuffKeldagrim;

	@Inject
	private CrowdsourcingManager manager;

	@Inject
	private Client client;

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SPAM)
		{
			
			if (message.startsWith("The barrel is now full"))
			{
				int regionID = -1;
				Player local = client.getLocalPlayer();
				if (local != null)
				{
					regionID = local.getWorldLocation().getRegionID();
				}

				int cookingLevel = client.getBoostedSkillLevel(Skill.COOKING);

				if (regionID == 11679) {
                    			BrewingData data = new BrewingData(message, regionID, this.theStuffKeldagrim, cookingLevel);
                		} else {
					BrewingData data = new BrewingData(message, regionID, this.theStuffPortPhasmatys, cookingLevel);
                		}

				//manager.storeEvent(data);
				System.out.print(message);
				System.out.print(regionID);
 				System.out.print(this.theStuffKeldagrim);
				System.out.print(this.theStuffPortPhasmatys);
				System.out.print(cookingLevel);
			}
		}

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged varbitChanged)
	{
		if (this.client.getVarbitValue(2294) == 1) {
			this.theStuffKeldagrim = true;
		} else {
			this.theStuffKeldagrim = false;
		}

		if (this.client.getVarbitValue(2295) == 1) {
			this.theStuffPortPhasmatys = true;
		} else {
			this.theStuffPortPhasmatys = false;
		}
	}
}
