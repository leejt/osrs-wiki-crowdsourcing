package com.Crowdsourcing;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AdvancedCrowdsourcingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AdvancedCrowdsourcingPlugin.class);
		RuneLite.main(args);
	}
}