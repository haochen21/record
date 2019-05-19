package com.betasoft.record.service;

import com.betasoft.record.builder.Queries;
import com.betasoft.record.builder.QueryBuilder;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

public interface DataPointService {

    Mono<Queries> query(QueryBuilder queryBuilder);

    Mono<Queries> find(String metric, String moc, List<String> mos, Date beginDate, Date endDate);

    Mono<Queries> avg(String metric, String moc, List<String> mos, Date beginDate, Date endDate);

    Mono<Queries> max(String metric, String moc, List<String> mos, Date beginDate, Date endDate);

    Mono<Queries> min(String metric, String moc, List<String> mos, Date beginDate, Date endDate);
}
