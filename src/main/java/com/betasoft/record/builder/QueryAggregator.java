package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QueryAggregator {

    @JsonProperty("name")
    private String name;

}
