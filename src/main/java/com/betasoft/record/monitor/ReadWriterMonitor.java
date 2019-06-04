package com.betasoft.record.monitor;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.service.MetricService;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Service
public class ReadWriterMonitor {

    private MetricService metricService;

    private LongAdder writeCounter = new LongAdder();

    private Timer timer;

    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public ReadWriterMonitor(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostConstruct
    public void init() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        timer = Timer.builder("record.query.timer")
                .publishPercentiles(0.95, 0.99) // median and 95th percentile
                .publishPercentileHistogram()
                .sla(Duration.ofSeconds(60))
                .register(registry);

        Flux.interval(Duration.ofSeconds(60), Schedulers.newSingle("ReadWriterMonitor"))
                .flatMap(i -> {
                    List<Metric> metrics = new ArrayList<>();

                    metrics.add(createMetric("record.write.count", new Double(writeCounter.sumThenReset())));

                    registry.getMeters()
                            .stream()
                            .filter(meter -> meter.getId().getTags().size() > 0)
                            .forEach(meter -> {
                                Metric metric = new Metric();
                                metric.setName(meter.getId().getName());

                                Map<String, String> tags = new HashMap<>();
                                for (Tag tag : meter.getId().getTags()) {
                                    tags.put(tag.getKey(), tag.getValue());
                                }
                                metric.setTags(tags);

                                List<Object[]> samplePoints = new ArrayList<>();

                                double value = new Double(decimalFormat.format(meter.measure().iterator().next().getValue()));
                                samplePoints.add(new Object[]{new Date().getTime(), value});
                                metric.setSamplePoints(samplePoints);
                                metric.setTtl(7 * 24 * 60 * 60);

                                metrics.add(metric);
                            });

                    return Flux.fromIterable(metrics);
                })
                .flatMap(metric -> metricService.saveMetrics(Flux.fromIterable(Arrays.asList(metric))))
                .subscribe();
    }

    public void write(long value) {
        writeCounter.add(value);
    }

    // milliseconds
    public void queryTime(long value) {
        timer.record(value, TimeUnit.MILLISECONDS);
    }

    private Metric createMetric(String metricName, double value) {
        Metric metric = new Metric();
        metric.setName(metricName);
        Map<String, String> tags = new HashMap<>();
        tags.put("module", "record");
        metric.setTags(tags);
        List<Object[]> samplePoints = new ArrayList<>();
        samplePoints.add(new Object[]{new Date().getTime(), value});
        metric.setSamplePoints(samplePoints);
        metric.setTtl(7 * 24 * 60 * 60);

        return metric;
    }

}
