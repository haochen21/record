package com.betasoft.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testFilterMetric() {
        Map<String,String> map = new HashMap<>();
        map.put("metric","OSCPU");
        webTestClient.post()
                .uri("/api/v1/metric")
                .body(Mono.just(map), Map.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(entityExchangeResult -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try{
                        String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entityExchangeResult.getResponseBody());
                        System.out.println(result);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                });
    }

    @Test
    public void testFindMoTypeByMetric() {
        Map<String,String> map = new HashMap<>();
        map.put("metric","OSCPU_CPU_LOAD");
        webTestClient.post()
                .uri("/api/v1/metric/moType")
                .body(Mono.just(map), Map.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(entityExchangeResult -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try{
                        String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entityExchangeResult.getResponseBody());
                        System.out.println(result);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                });
    }

    @Test
    public void testFindMoByMetricAndMoType() {
        Map<String,String> map = new HashMap<>();
        map.put("metric","OSCPU_CPU_LOAD");
        map.put("moType","Windows");
        webTestClient.post()
                .uri("/api/v1/metric/moId")
                .body(Mono.just(map), Map.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(entityExchangeResult -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try{
                        String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entityExchangeResult.getResponseBody());
                        System.out.println(result);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                });
    }
}
