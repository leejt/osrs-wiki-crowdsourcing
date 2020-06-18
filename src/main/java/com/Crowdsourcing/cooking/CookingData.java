package com.Crowdsourcing.cooking;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CookingData
{
    private final String message;
    private final boolean hasCookingGauntlets;
    private final boolean inHosidiusKitchen;
    private final boolean kourendElite;
    private final int lastGameObjectClicked;
    private final int level;
}
