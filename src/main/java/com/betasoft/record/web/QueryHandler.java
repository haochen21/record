package com.betasoft.record.web;

import com.betasoft.record.builder.Queries;
import com.betasoft.record.builder.QueryBuilder;
import com.betasoft.record.service.DataPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class QueryHandler {

    private DataPointService dataPointService;

    @Autowired
    public QueryHandler(DataPointService dataPointService) {
        this.dataPointService = dataPointService;
    }

    public Mono<ServerResponse> query(ServerRequest serverRequest) {
        Mono<QueryBuilder> queryBuilderMono = serverRequest.bodyToMono(QueryBuilder.class);
        Mono<Queries> queriesMono = dataPointService.query(queryBuilderMono);

        return queriesMono
                .elapsed()
                .flatMap(tuple2 ->
                        ServerResponse.status(HttpStatus.OK)
                                .body(Mono.just(tuple2.getT2()), Queries.class));

    }
}
