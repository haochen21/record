package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.DataPoint;
import reactor.core.publisher.Flux;

public interface MetricService {

    Flux<DataPoint> saveMetrics(Flux<Metric> metricFlux);
}
