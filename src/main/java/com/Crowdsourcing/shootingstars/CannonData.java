package com.Crowdsourcing.shootingstars;

import lombok.Value;
import net.runelite.client.config.RuneScapeProfileType;

@Value
public class CannonData
{
	int cannonVarbit;
	long time;
	int world;
	RuneScapeProfileType mode;
}
