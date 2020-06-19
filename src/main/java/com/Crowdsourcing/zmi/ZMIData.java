package com.Crowdsourcing.zmi;

import com.google.common.collect.Multiset;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZMIData
{
	private final int tickDelta;
	private final boolean ardougneMedium;
	private final int level;
	private final int xpGained;
	private final Multiset<Integer> itemsReceived;
	private final Multiset<Integer> itemsRemoved;

	//printout for debug purposes
	public String toString()
	{
		StringBuilder strItemsReceived = new StringBuilder();
		for (Multiset.Entry<Integer> entry : itemsReceived.entrySet())
		{
			strItemsReceived
				.append(entry.getElement())
				.append(entry.getElement().toString().length() > 3 ? "\t" : "\t\t")
				.append(entry.getCount())
				.append("\n");
		}

		StringBuilder strItemsRemoved = new StringBuilder();
		for (Multiset.Entry<Integer> entry : itemsRemoved.entrySet())
		{
			strItemsRemoved
				.append(entry.getElement())
				.append(entry.getElement().toString().length() > 3 ? "\t" : "\t\t")
				.append(entry.getCount())
				.append("\n");
		}

		return ("\n=== RUNECRAFT DATA === \nTick Delta: " + tickDelta + "\nHas Ardougne Medium: " + ardougneMedium + "\nRunecraft Level: " + level +
			"\nXP Gained: " + xpGained + "\nItems Received:\nITEM\tQTY\n" + strItemsReceived + "Items Removed:\nITEM\tQTY\n" + strItemsRemoved);
	}
}