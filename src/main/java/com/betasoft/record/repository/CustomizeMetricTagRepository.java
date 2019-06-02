package com.betasoft.record.repository;

import reactor.core.publisher.Mono;

public interface CustomizeMetricTagRepository {

    Mono<Boolean> create(String metricName, String tagKey, String tagValue);

    Mono<Boolean> addTagValue(String metricName, String tagKey, String tagValue);
}
