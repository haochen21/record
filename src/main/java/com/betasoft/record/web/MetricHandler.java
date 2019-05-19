package com.betasoft.record.web;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.service.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MetricHandler {

    private MetricService metricService;

    @Autowired
    public MetricHandler(MetricService metricService){
        this.metricService = metricService;
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        Flux<Metric> metricSaved = serverRequest.bodyToFlux(Metric.class);
        metricService.saveMetrics(metricSaved);
        return ServerResponse.status(HttpStatus.OK).body(Mono.empty(),Void.class);
    }
}
