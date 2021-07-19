package com.Crowdsourcing.clues;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ClueData
{
    private int clueId;
    private String text;
    private List<MapClueWidgetPart> parts;
}