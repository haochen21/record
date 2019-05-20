package com.betasoft.record.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DataPointWrapper {

    private DataPoint dataPoint;

    private int ttl;

    private String metricName;

    private String category;

    private String moPath;
}
