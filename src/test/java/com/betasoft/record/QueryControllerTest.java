package com.betasoft.record;

import com.betasoft.record.builder.Queries;
import com.betasoft.record.builder.QueryBuilder;
import com.betasoft.record.builder.QueryMetric;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QueryControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void queryFind() throws Exception {
        List<String> mos = new ArrayList<>();
        mos.add("localhost");
        mos.add("127.0.0.1");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        QueryBuilder queryBuilder = new QueryBuilder();

        Date beginDate = sdf.parse(" 2019-01-16 16:14:59");
        Date endDate = sdf.parse(" 2019-02-17 08:13:59");
        queryBuilder.setBeginDate(beginDate.getTime());
        queryBuilder.setEndDate(endDate.getTime());

        QueryMetric queryMetric = new QueryMetric();
        queryMetric.setName("OSCPU_CPU_LOAD");

        Map<String, Set<String>> tags = new HashMap<>();
        tags.put("moc", new HashSet<>(Arrays.asList("Windows")));
        tags.put("mo", new HashSet<>(Arrays.asList("21.0.0.0", "30.0.0.56","127.0.0.1")));
        queryMetric.setTags(tags);

        queryBuilder.setMetrics(Arrays.asList(queryMetric));

        webTestClient.post()
                .uri("/api/v1/datapoints/query")
                .body(Mono.just(queryBuilder), QueryBuilder.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Queries.class)
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
