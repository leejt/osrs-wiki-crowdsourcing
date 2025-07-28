package com.Crowdsourcing.toa;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TombsMiningData
{
	private final Pickaxe pickaxe;
	private final int baseMiningLevel;
	private final int boostedMiningLevel;
	private final int damage;
	private final boolean isMaxHit;
}
