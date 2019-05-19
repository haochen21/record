package com.betasoft.record.repository;

import com.betasoft.record.builder.AggregatorPoint;
import com.betasoft.record.model.DataPoint;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;

public interface CustomizeDataPointRepository {

    Flux<AggregatorPoint> avg(String metric, String moc, List<String> mos, Date beginDate, Date endDate);

    Flux<AggregatorPoint> max(String metric, String moc, List<String> mos, Date beginDate, Date endDate);

    Flux<AggregatorPoint> min(String metric, String moc, List<String> mos, Date beginDate, Date endDate);

    Flux<DataPoint> findSamplePoints(String metric, String moc, List<String> mos, Date beginDate, Date endDate);
}
