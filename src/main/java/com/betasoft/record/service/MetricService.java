package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MetricService {

    Mono<Long> saveMetrics(Flux<Metric> metricFlux);
}
