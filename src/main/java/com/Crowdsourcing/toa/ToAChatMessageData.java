package com.Crowdsourcing.toa;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ToAChatMessageData
{
	private final int gameTickToA;
	private final String chatMessageToA;
}
