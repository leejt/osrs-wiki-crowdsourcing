package com.Crowdsourcing.quest_log;

import com.Crowdsourcing.CrowdsourcingManager;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class CrowdsourcingQuestLog
{

	@Inject
	private Client client;

	@Inject
	private CrowdsourcingManager manager;

	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		log.info("Hello {}", client.getGameState());
		Widget w = client.getWidget(WidgetInfo.DIARY_QUEST_WIDGET_TEXT);
		if (w != null && w.getStaticChildren() != null)
			log.info("" + w.getStaticChildren().length);

		log.info("" + w.getId());
		if (w.getStaticChildren() != null) {
			for(Widget child : w.getStaticChildren()) {
				log.info(child.getText());
			}
		}

		Widget widget = client.getWidget(WidgetInfo.DIARY_QUEST_WIDGET_TEXT);
		Widget[] children = widget.getStaticChildren();

		Widget titleWidget = children[0];
		if (titleWidget == null)
		{
			return;
		}
	}

}
