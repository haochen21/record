package com.betasoft.record.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class SamplePoint {

    private long timestamp;

    private double value;
}
