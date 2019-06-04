package com.betasoft.record.web;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.monitor.ReadWriterMonitor;
import com.betasoft.record.service.MetricService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MetricHandler {

    private MetricService metricService;

    private ReadWriterMonitor readWriterMonitor;

    public MetricHandler(MetricService metricService, ReadWriterMonitor readWriterMonitor) {
        this.metricService = metricService;
        this.readWriterMonitor = readWriterMonitor;
    }

    public Mono<ServerResponse> save(ServerRequest serverRequest) {
        Flux<Metric> metricSaved = serverRequest.bodyToFlux(Metric.class);
        Mono<Long> dataPointNum = metricService.saveMetrics(metricSaved)
                .doOnNext(size -> readWriterMonitor.write(size));
        return ServerResponse.status(HttpStatus.OK).body(dataPointNum, Long.class);
    }
}
