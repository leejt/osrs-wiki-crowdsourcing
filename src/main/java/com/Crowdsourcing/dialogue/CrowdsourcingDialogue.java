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

package com.Crowdsourcing.dialogue;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import com.Crowdsourcing.CrowdsourcingManager;

@Slf4j
public class CrowdsourcingDialogue
{

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	private String lastSpriteText = null;
	private int lastItemId;

	private String lastDoubleSpriteText = null;
	private int lastDoubleItemId1;
	private int lastDoubleItemId2;

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		Widget spriteWidget = client.getWidget(WidgetInfo.DIALOG_SPRITE_SPRITE);
		Widget textWidget = client.getWidget(WidgetInfo.DIALOG_SPRITE_TEXT);
		if (spriteWidget != null && textWidget != null && (!textWidget.getText().equals(lastSpriteText)
			|| spriteWidget.getItemId() != lastItemId))
		{
			lastItemId = spriteWidget.getItemId();
			lastSpriteText = textWidget.getText();
			log.debug(String.format("%d: %s", lastItemId, lastSpriteText));
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
			SpriteTextData data = new SpriteTextData(lastSpriteText, lastItemId, isInInstance, location);
			manager.storeEvent(data);
		}
		else if (spriteWidget == null || textWidget == null)
		{
			lastSpriteText = null;
			lastItemId = -1;
		}


		Widget doubleSprite1Widget = client.getWidget(11, 1);
		Widget doubleSprite2Widget = client.getWidget(11, 3);
		Widget doubleTextWidget = client.getWidget(11, 2);
		if (doubleSprite1Widget != null && doubleTextWidget != null && (!doubleTextWidget.getText().equals(lastDoubleSpriteText)
				|| doubleSprite1Widget.getItemId() != lastDoubleItemId1 || doubleSprite2Widget.getItemId() != lastDoubleItemId2))
		{
			lastDoubleItemId1 = doubleSprite1Widget.getItemId();
			lastDoubleItemId2 = doubleSprite2Widget.getItemId();
			lastDoubleSpriteText = doubleTextWidget.getText();
			log.debug(String.format("%d, %d: %s", lastDoubleItemId1, lastDoubleItemId2, lastDoubleSpriteText));
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
			DoubleSpriteTextData data = new DoubleSpriteTextData(lastDoubleSpriteText, lastDoubleItemId1, lastDoubleItemId2, isInInstance, location);
			manager.storeEvent(data);
		}
		else if (doubleSprite1Widget == null || doubleSprite2Widget == null || doubleTextWidget == null)
		{
			lastDoubleSpriteText = null;
			lastDoubleItemId1 = -1;
			lastDoubleItemId2 = -1;
		}
	}
}
