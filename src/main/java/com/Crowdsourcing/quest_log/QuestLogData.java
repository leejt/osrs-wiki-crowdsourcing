package com.Crowdsourcing.quest_log;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestLogData
{
	private String qlName;
	private boolean qlIsVarbit;
	private int qlVarbIndex;
	private int qlVarbValue;
	private String qlText;
}
