package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QueryBuilder {

    @JsonProperty("start_absolute")
    private long beginDate;

    @JsonProperty("end_absolute")
    private long endDate;

    // 目前只支持一个查询指标
    @JsonProperty("metrics")
    private List<QueryMetric> metrics = new ArrayList<>();

}
