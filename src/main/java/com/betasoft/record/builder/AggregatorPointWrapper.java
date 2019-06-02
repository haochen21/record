package com.betasoft.record.builder;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class AggregatorPointWrapper {

    private AggregatorPointKey aggregatorPointKey;

    private double value;
}
