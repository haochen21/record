package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.Mo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface MoService {

    Mono<Mo> findByMetricAndMo(Metric metric);

    Mono<Metric> saveMo(Metric metric);

    Flux<String> filterMetric(Mono<Map> metricMono);

    Flux<String> findMoTypeByMetric(Mono<Map> metricMono);

    Flux<String> findMoByMetricAndMoType(Mono<Map> metricMono);
}
