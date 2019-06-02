package com.betasoft.record.builder;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class AggregatorPoint {

    private String metric;

    private String tagJson;

    private String day;

    @NonNull
    private double value;
}
