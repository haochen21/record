package com.betasoft.record.web;

import com.betasoft.record.service.MoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
        return moService.filterMetric(queryMap)
                .map(list -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.writeValueAsString(list);
                    } catch (Exception ex) {
                        return "[]";
                    }
                }).flatMap(json -> ServerResponse.ok()
                        .body(Mono.just(json), String.class)
                );
    }

    public Mono<ServerResponse> findMoTypeByMetric(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return moService.findMoTypeByMetric(queryMap)
                .map(list -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.writeValueAsString(list);
                    } catch (Exception ex) {
                        return "[]";
                    }
                }).flatMap(json -> ServerResponse.ok()
                        .body(Mono.just(json), String.class)
                );
    }

    public Mono<ServerResponse> findMoByMetricAndMoType(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return moService.findMoByMetricAndMoType(queryMap)
                .map(list -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.writeValueAsString(list);
                    } catch (Exception ex) {
                        return "[]";
                    }
                }).flatMap(json -> ServerResponse.ok()
                        .body(Mono.just(json), String.class)
                );
    }
}
