package com.betasoft.record.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ApplicationRoutes {

    //https://github.com/Ardeshir1988/WebFlux-Reactive-Cassandra
    //https://github.com/bijukunjummen/sample-webflux-func-cassandra/blob/master/src/main/java/cass/service/HotelService.java
    @Bean
    public RouterFunction<ServerResponse> routes(MetricHandler metricHandler,QueryHandler queryHandler,MoHander moHander) {
        return RouterFunctions
                .route(POST("/api/v1/datapoints").and(accept(APPLICATION_JSON)), metricHandler::save)
                .andRoute(POST("/api/v1/datapoints/query").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), queryHandler::query)
                .andRoute(POST("/api/v1/metric").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), moHander::filterMetric)
                .andRoute(POST("/api/v1/metric/moType").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), moHander::findMoTypeByMetric)
                .andRoute(POST("/api/v1/metric/moId").and(accept(APPLICATION_JSON)).and(contentType(APPLICATION_JSON)), moHander::findMoByMetricAndMoType);

    }
}
