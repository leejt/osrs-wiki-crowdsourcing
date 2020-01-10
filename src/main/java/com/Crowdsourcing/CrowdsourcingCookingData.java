package com.Crowdsourcing;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class CrowdsourcingCookingData {
    private final String message;
    private final boolean hasCookingGauntlets;
    private final boolean inHosidiusKitchen;
    private final boolean kourendElite;
    private final String animation;
    private final int level;
}
