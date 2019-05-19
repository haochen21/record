package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Query {

    private List<Result> resultList;

    public Query(List<Result> resultList) {
        this.resultList = resultList;
    }

    @JsonProperty("results")
    public List<Result> getResultsList() {
        return resultList;
    }
}
