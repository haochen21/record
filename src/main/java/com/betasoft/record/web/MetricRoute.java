package com.betasoft.record.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class MetricRoute {

    //https://github.com/Ardeshir1988/WebFlux-Reactive-Cassandra
    //https://github.com/bijukunjummen/sample-webflux-func-cassandra/blob/master/src/main/java/cass/service/HotelService.java
    @Bean
    public RouterFunction<ServerResponse> routes(MetricHandler metricHandler) {
        return nest(path("/api/v1/datapoints"),
                nest(accept(MediaType.APPLICATION_JSON),
                        route(POST("/"), metricHandler::save)
                ));
    }
}
