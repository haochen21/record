package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {

    private long beginDate;

    private long endDate;

    // 目前只支持一个查询指标
    private List<QueryMetric> metrics = new ArrayList<>();

    public QueryBuilder() {
    }

    @JsonProperty("start_absolute")
    public long getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(long beginDate) {
        this.beginDate = beginDate;
    }

    @JsonProperty("end_absolute")
    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("metrics")
    public List<QueryMetric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<QueryMetric> metrics) {
        this.metrics = metrics;
    }
}
