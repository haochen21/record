package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.Mo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface MoService {

    Mono<Mo> findByMetricAndMo(Metric metric);

    Mono<Metric> saveMo(Metric metric);

    Mono<List<String>> filterMetric(Mono<Map> metricMono);

    Mono<List<String>> findMoTypeByMetric(Mono<Map> metricMono);

    Mono<List<String>> findMoByMetricAndMoType(Mono<Map> metricMono);
}
