package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.DataPointWithTtl;
import reactor.core.publisher.Flux;

public interface MetricService {

    Flux<DataPointWithTtl> saveMetrics(Flux<Metric> metricFlux);
}
