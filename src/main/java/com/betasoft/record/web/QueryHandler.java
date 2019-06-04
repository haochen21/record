package com.betasoft.record.web;

import com.betasoft.record.builder.Queries;
import com.betasoft.record.builder.QueryBuilder;
import com.betasoft.record.monitor.ReadWriterMonitor;
import com.betasoft.record.service.DataPointService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class QueryHandler {

    private DataPointService dataPointService;

    private ReadWriterMonitor readWriterMonitor;

    public QueryHandler(DataPointService dataPointService,ReadWriterMonitor readWriterMonitor) {
        this.dataPointService = dataPointService;
        this.readWriterMonitor = readWriterMonitor;
    }

    public Mono<ServerResponse> query(ServerRequest serverRequest) {
        Mono<QueryBuilder> queryBuilderMono = serverRequest.bodyToMono(QueryBuilder.class);
        Mono<Queries> queriesMono = dataPointService.query(queryBuilderMono);

        return queriesMono
                .elapsed()
                .doOnNext(tuple2 -> {
                    readWriterMonitor.queryTime(tuple2.getT1());
                })
                .flatMap(tuple2 ->
                        ServerResponse.status(HttpStatus.OK)
                                .header("executetime",tuple2.getT1().toString())
                                .body(Mono.just(tuple2.getT2()), Queries.class));

    }
}
