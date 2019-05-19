package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryAggregator {

    private String name;

    public QueryAggregator(){

    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
