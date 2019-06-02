package com.betasoft.record.service;

import com.betasoft.record.builder.Queries;
import com.betasoft.record.builder.QueryBuilder;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DataPointService {

    Mono<Queries> query(Mono<QueryBuilder> queryBuilder);

    Mono<Queries> find(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate);

    Mono<Queries> avg(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate);

    Mono<Queries> max(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate);

    Mono<Queries> min(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate);
}
