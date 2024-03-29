package com.Crowdsourcing.hunter_rumours;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HunterRumourData {
    private String hunterName;
    private String creatureName;
    private int killCount;
}
