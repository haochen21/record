package com.betasoft.record;

import com.betasoft.record.builder.Metric;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MetricControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testSaveMetrics() {
        List<Metric> metrics = new ArrayList<>();

        metrics.add(createMetric("OSCPU_CPU_LOAD", "host", "Windows", "10001"));
        metrics.add(createMetric("OSCPU_CPU_LOAD", "host", "NetworkDevice", "10002"));
        metrics.add(createMetric("OSCPU_CPU_LOAD", "host", "Windows", "10003"));

        metrics.add(createMetric("CPU_LOAD", "host", "Windows", "10001"));
        metrics.add(createMetric("CPU_LOAD", "host", "NetworkDevice", "10002"));

        webTestClient.post()
                .uri("/api/v1/datapoints")
                .body(Flux.fromIterable(metrics), Metric.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class)
                .isEqualTo(new Long(5));
    }

    private Metric createMetric(String metricName, String category, String moType, String moId) {
        Random random = new Random();
        Calendar nowCalendar = Calendar.getInstance();

        Metric metric = new Metric();
        metric.setName(metricName);
        Map<String, String> tags = new HashMap<>();
        tags.put("moc", category);

        StringBuilder moPathSb = new StringBuilder();
        moPathSb.append(category).append(".").append(moType).append(",uuid=\"").append(moId).append("\"");
        tags.put("mo", moPathSb.toString());
        metric.setTags(tags);

        List<Object[]> samplePoints = new ArrayList<>();
        metric.setSamplePoints(samplePoints);
        for (int i = 0; i < 1; i++) {
            Calendar sampleCalendar = (Calendar) nowCalendar.clone();
            sampleCalendar.add(Calendar.MINUTE, i * 5);
            samplePoints.add(new Object[]{sampleCalendar.getTime().getTime(), random.nextDouble()});
        }

        metric.setTtl(3 * 60 * 1000);
        return metric;
    }
}
