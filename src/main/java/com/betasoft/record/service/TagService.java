package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface TagService {

    Mono<Metric> saveMetricTag(Metric metric);

    Mono<List<String>> filterMetric(Mono<Map> metricMono);

    Mono<List<String>> findTagKeyByMetric(Mono<Map> metricMono);

    Mono<List<String>> findTagValueByMetricAndTagKey(Mono<Map> metricMono);
}
