package com.Crowdsourcing.dialogue;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class NpcDialogueData {
	private final String message;
	private final String name;
}