package com.betasoft.record.repository;

import com.betasoft.record.builder.AggregatorPoint;
import com.betasoft.record.model.DataPoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

public interface CustomizeDataPointRepository {

    Mono<List<AggregatorPoint>> avg(String metric, List<String> tagJsons, Date beginDate, Date endDate);

    Mono<List<AggregatorPoint>> max(String metric, List<String> tagJsons, Date beginDate, Date endDate);

    Mono<List<AggregatorPoint>> min(String metric, List<String> tagJsons, Date beginDate, Date endDate);

    Flux<DataPoint> findSamplePoints(String metric, List<String> tagJsons, Date beginDate, Date endDate);
}
