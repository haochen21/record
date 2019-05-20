package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Query {

    @JsonProperty("results")
    private List<Result> resultList;

    public Query(List<Result> resultList) {
        this.resultList = resultList;
    }

}
