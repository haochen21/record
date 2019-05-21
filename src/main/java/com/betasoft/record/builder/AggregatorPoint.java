package com.betasoft.record.builder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AggregatorPoint {

    private String metric;

    private String moType;

    private String moId;

    private String day;

    private double value;
}
