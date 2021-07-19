package com.Crowdsourcing.clues;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MapClueWidgetPart
{
    private int x;
    private int y;
    private int rotationX;
    private int rotationY;
    private int rotationZ;
    private int modelId;
    private int modelZoom;
}