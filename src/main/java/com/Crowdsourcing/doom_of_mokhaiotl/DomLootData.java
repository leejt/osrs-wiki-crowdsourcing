package com.Crowdsourcing.doom_of_mokhaiotl;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DomLootData
{
	private List<Map<Integer, Integer>> waveLoot;
}
