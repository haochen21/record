package com.betasoft.record.web;

import com.betasoft.record.service.TagService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class MoHander {

    private TagService moService;

    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    public MoHander(TagService moService) {
        this.moService = moService;
    }

    public Mono<ServerResponse> filterMetric(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return moService.filterMetric(queryMap)
                .flatMap(this::write)
                .flatMap(json -> ServerResponse.ok()
                        .body(Mono.just(json), String.class)
                ).onErrorResume(
                        JsonProcessingException.class,
                        (e) -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Mono.just(e.getMessage()), String.class)
                );
    }

    public Mono<ServerResponse> findTagKeyByMetric(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return moService.findTagKeyByMetric(queryMap)
                .flatMap(this::write)
                .flatMap(json -> ServerResponse.ok()
                        .body(Mono.just(json), String.class)
                ).onErrorResume(
                        JsonProcessingException.class,
                        (e) -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Mono.just(e.getMessage()), String.class)
                );
    }

    public Mono<ServerResponse> findTagValueByMetricAndTagKey(ServerRequest serverRequest) {
        Mono<Map> queryMap = serverRequest.bodyToMono(Map.class);
        return moService.findTagValueByMetricAndTagKey(queryMap)
                .flatMap(this::write)
                .flatMap(json -> ServerResponse.ok()
                        .body(Mono.just(json), String.class)
                ).onErrorResume(
                        JsonProcessingException.class,
                        (e) -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Mono.just(e.getMessage()), String.class)
                );
    }

    private Mono<String> write(Object value) {
        try {
            return Mono.just(JSON.writeValueAsString(value));
        } catch (JsonProcessingException ex) {
            return Mono.error(ex);
        }
    }
}
