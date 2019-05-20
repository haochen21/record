package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class QueryMetric {

    // 指标名称
    @JsonProperty("name")
    private String name;

    // 要查询的mo
    @JsonProperty("tags")
    private Map<String, Set<String>> tags;

    // 函数名称
    @JsonProperty("aggregators")
    private List<QueryAggregator> aggregators;
}
