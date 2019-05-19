package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Queries {

    private List<Query> queryList;

    public Queries(List<Query> queryList) {
        this.queryList = queryList;
    }

    @JsonProperty("queries")
    public List<Query> getQueryList() {
        return queryList;
    }
}
