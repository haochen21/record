package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Queries {

    @JsonProperty("queries")
    private List<Query> queryList;

    public Queries(List<Query> queryList) {
        this.queryList = queryList;
    }
}
