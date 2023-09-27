package com.Crowdsourcing.shootingstars;

import lombok.Value;
import net.runelite.client.config.RuneScapeProfileType;

@Value
public class TelescopeData
{
	private int world;
	private String message;
	private RuneScapeProfileType mode;
}
