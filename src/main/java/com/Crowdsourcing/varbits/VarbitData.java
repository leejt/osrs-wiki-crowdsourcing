package com.Crowdsourcing.varbits;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class VarbitData
{
	private final int varbitNumber;
	private final int oldValue;
	private final int newValue;
	private final int tick;
	private final WorldPoint location;
}
