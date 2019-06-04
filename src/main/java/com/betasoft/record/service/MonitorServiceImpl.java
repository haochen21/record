package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

@Service
public class MonitorServiceImpl implements MonitorService {

    MeterRegistry meterRegistry;

    MetricService metricService;

    public MonitorServiceImpl(MeterRegistry meterRegistry, MetricService metricService) {
        this.meterRegistry = meterRegistry;
        this.metricService = metricService;
    }

    ScheduledThreadPoolExecutor executor;

    @PostConstruct
    public void init() {
        ThreadFactory threadFactory = new MonitorThreadFactory();
        executor = new ScheduledThreadPoolExecutor(2, threadFactory);

        executor.scheduleAtFixedRate(() -> monitor(),
                2000, 5 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void monitor() {
        List<Metric> metrics = new ArrayList<>();

        jvmMemoryUsed(metrics);
        systemCpuUsage(metrics);
        processCpuUsage(metrics);

        metricService.saveMetrics(Flux.fromIterable(metrics)).subscribe();
    }

    private void jvmMemoryUsed(List<Metric> metrics) {
        Collection<Meter> meters = meterRegistry.find("jvm.memory.used").meters();
        Map<Statistic, Double> samples = getSamples(meters);
        samples.forEach((key, value) -> {
            DecimalFormat decimalFormat = new DecimalFormat("#");
            value = value / 1024 / 1024;
            Metric metric = createMetric("jvm.memory.used", new Double(decimalFormat.format(value)));
            metrics.add(metric);
        });
    }

    private void systemCpuUsage(List<Metric> metrics) {
        Collection<Meter> meters = meterRegistry.find("system.cpu.usage").meters();
        Map<Statistic, Double> samples = getSamples(meters);
        samples.forEach((key, value) -> {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            if (value != 0.0) {
                value = value * 100;
                Metric metric = createMetric("system.cpu.usage", new Double(decimalFormat.format(value)));
                metrics.add(metric);
            }
        });
    }

    private void processCpuUsage(List<Metric> metrics) {
        Collection<Meter> meters = meterRegistry.find("process.cpu.usage").meters();
        Map<Statistic, Double> samples = getSamples(meters);
        samples.forEach((key, value) -> {
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            if (value != 0.0) {
                value = value * 100;
                Metric metric = createMetric("process.cpu.usage", new Double(decimalFormat.format(value)));
                metrics.add(metric);
            }
        });
    }

    private Map<Statistic, Double> getSamples(Collection<Meter> meters) {
        Map<Statistic, Double> samples = new LinkedHashMap<>();
        meters.forEach((meter) -> mergeMeasurements(samples, meter));
        return samples;
    }

    private void mergeMeasurements(Map<Statistic, Double> samples, Meter meter) {
        meter.measure().forEach((measurement) -> samples.merge(measurement.getStatistic(),
                measurement.getValue(), mergeFunction(measurement.getStatistic())));
    }

    private BiFunction<Double, Double, Double> mergeFunction(Statistic statistic) {
        return Statistic.MAX.equals(statistic) ? Double::max : Double::sum;
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

    private static class MonitorThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        private static final String MonitorPoolerThreadName = "Monitor";

        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName(MonitorPoolerThreadName + "-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    }
}
