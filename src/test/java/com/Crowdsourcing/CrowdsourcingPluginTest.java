package com.Crowdsourcing;

import com.Crowdsourcing.CrowdsourcingPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CrowdsourcingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CrowdsourcingPlugin.class);
		RuneLite.main(args);
	}
}