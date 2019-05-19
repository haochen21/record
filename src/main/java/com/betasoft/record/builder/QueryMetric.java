package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryMetric {

    // 指标名称
    private String name;

    // 要查询的mo
    private Map<String, Set<String>> tags;

    // 函数名称
    private List<QueryAggregator> aggregators;

    public QueryMetric(){

    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("tags")
    public Map<String, Set<String>> getTags() {
        return tags;
    }

    public void setTags(Map<String, Set<String>> tags) {
        this.tags = tags;
    }

    @JsonProperty("aggregators")
    public List<QueryAggregator> getAggregators() {
        return aggregators;
    }

    public void setAggregators(List<QueryAggregator> aggregators) {
        this.aggregators = aggregators;
    }
}
