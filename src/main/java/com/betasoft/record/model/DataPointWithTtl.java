package com.betasoft.record.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DataPointWithTtl {

    private DataPoint dataPoint;

    private int ttl;
}
