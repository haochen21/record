package com.betasoft.record.repository;

import com.betasoft.record.builder.AggregatorPoint;
import com.betasoft.record.model.DataPoint;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;

public interface CustomizeDataPointRepository {

    Flux<AggregatorPoint> avg(String metric, String moType, List<String> moIds, Date beginDate, Date endDate);

    Flux<AggregatorPoint> max(String metric, String moType, List<String> moIds, Date beginDate, Date endDate);

    Flux<AggregatorPoint> min(String metric, String moType, List<String> moIds, Date beginDate, Date endDate);

    Flux<DataPoint> findSamplePoints(String metric, String moType, List<String> moIds, Date beginDate, Date endDate);
}
