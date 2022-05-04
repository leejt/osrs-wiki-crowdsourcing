package com.Crowdsourcing.mahogany_homes;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.runelite.api.coords.WorldPoint;

@Data
@AllArgsConstructor
public class MahoganyHomesData
{
	private String mhContractorName;
	private String mhHomeownerName;
	private int mhTier;
	private int mhNumberFixed;
}
