package com.betasoft.record.web;

import com.betasoft.record.service.MoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class MoHander {

    private MoService moService;

    @Autowired
    public MoHander(MoService moService){
        this.moService = moService;
    }

    public Mono<ServerResponse> filterMetric(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return ServerResponse.status(HttpStatus.OK).body(moService.filterMetric(queryMap), String.class);
    }

    public Mono<ServerResponse> findMoTypeByMetric(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return ServerResponse.status(HttpStatus.OK).body(moService.findMoTypeByMetric(queryMap),String.class);
    }

    public Mono<ServerResponse> findMoByMetricAndMoType(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return ServerResponse.status(HttpStatus.OK).body(moService.findMoByMetricAndMoType(queryMap),String.class);
    }
}
