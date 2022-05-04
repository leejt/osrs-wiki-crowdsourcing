package com.Crowdsourcing.mahogany_homes;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class MahoganyHomesData
{
	// Should we return specific info about the varbs changed?
	// Should we include location assigned? Does your assignment possibly depend on your location so you don't get the same one twice?
	// Seems unlikely.
	private String mhContractorName;
	private String mhHomeownerName;
	private int mhTier;
	private int mhNumberFixed;
}
