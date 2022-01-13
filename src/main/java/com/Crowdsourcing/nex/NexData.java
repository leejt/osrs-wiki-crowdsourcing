package com.Crowdsourcing.nex;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NexData
{
    List<NexDataEntry> nexData;
    int version;
}
