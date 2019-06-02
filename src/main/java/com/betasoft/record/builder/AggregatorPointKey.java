package com.betasoft.record.builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AggregatorPointKey {

    private String metric;

    private String tagJson;
}
