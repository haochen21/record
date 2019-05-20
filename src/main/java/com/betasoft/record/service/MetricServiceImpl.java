package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.*;
import com.betasoft.record.repository.DataPointRepository;
import com.betasoft.record.repository.MoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class MetricServiceImpl implements MetricService {

    @Autowired
    DataPointRepository dataPointRepository;

    @Autowired
    MoRepository moRepository;

    //metric:category:mos
    ConcurrentHashMap<String, ConcurrentHashMap<String, List<Mo>>> moMap;

    @PostConstruct
    public void init() {
        moRepository.findAll().collectList().subscribe(mos -> {
            moMap = mos.stream()
                    .collect(Collectors.groupingBy(mo ->
                                    mo.getMoKey().getMetric(),
                            ConcurrentHashMap::new,
                            Collectors.groupingBy(mo ->
                                            mo.getMoKey().getCategory(),
                                    ConcurrentHashMap::new,
                                    toList()
                            )
                    ));
            System.out.println(moMap.toString());
            moMap.clear();
        });

    }

    @Override
    public Mono<Long> saveMetrics(Flux<Metric> metricFlux) {
        Flux<Metric> filterMetricFlux = metricFlux.filter(metric -> {
            if (metric.getTags() == null || metric.getTags().size() == 0) {
                return false;
            }
            // 必需包含moc属性
            if (!metric.getTags().containsKey("moc")) {
                return false;
            }
            // 必需包含mo属性
            if (!metric.getTags().containsKey("mo")) {
                return false;
            }
            // 必需包含采集的时候以及值
            if (metric.getSamplePoints() == null || metric.getSamplePoints().size() == 0) {
                return false;
            }
            return true;
        });

        return filterMetricFlux
                .flatMap(metric -> saveMo(metric).map(metric1 -> metric1))
                .flatMap(this::getDataPoint)
                .flatMap(dataPointWrapper ->
                        dataPointRepository.insert(dataPointWrapper.getDataPoint(), dataPointWrapper.getTtl()))
                .count();

    }

    public Mono<Mo> findByMetricAndMo(Metric metric) {
        String metricName = metric.getName();
        String category = metric.getTags().get("moc");
        String moPath = metric.getTags().get("mo");
        Optional<Mo> moOptional = moMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(metricName))
                .map(entry -> entry.getValue())
                .flatMap(entry1 -> entry1.entrySet().stream())
                .filter(entry -> entry.getKey().equals(category))
                .map(entry -> entry.getValue())
                .flatMap(List::stream)
                .filter(mo -> mo.getMoKey().getMoPath().equals(moPath))
                .findAny();
        return Mono.justOrEmpty(moOptional);
    }

    private Flux<DataPointWrapper> getDataPoint(Metric metric) {
        String metricName = metric.getName();
        String category = metric.getTags().get("moc");
        String moPath = metric.getTags().get("mo");
        int ttl = metric.getTtl();

        return Flux.fromIterable(metric.getSamplePoints())
                .map(samplePoint -> {
                    Date eventTime = new Date((Long) samplePoint[0]);
                    Double value = Double.parseDouble(samplePoint[1].toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    DataPointKey dataPointKey = new DataPointKey(metricName, moPath, sdf.format(eventTime), eventTime);
                    metric.getTags().remove("mo");
                    DataPoint dataPoint = new DataPoint(dataPointKey, value, metric.getTags());
                    dataPoint.setDataPointKey(dataPointKey);

                    DataPointWrapper dataPointWrapper = new DataPointWrapper(dataPoint, ttl, metricName, category, moPath);
                    return dataPointWrapper;
                });
    }

    private Mono<Metric> saveMo(Metric metric) {
        return findByMetricAndMo(metric)
                .switchIfEmpty(Mono.defer(() -> {
                    String metricName = metric.getName();
                    String category = metric.getTags().get("moc");
                    String moPath = metric.getTags().get("mo");
                    MoKey metricMoKey = new MoKey(metric.getName(), category, moPath);
                    Mo mo = new Mo();
                    mo.setMoKey(metricMoKey);
                    for (Map.Entry<String, String> entry : metric.getTags().entrySet()) {
                        if (!entry.getKey().equals("mo") &&
                                !entry.getKey().equals("category")) {
                            mo.getTags().put(entry.getKey(), entry.getValue());
                        }
                    }
                    moMap.computeIfAbsent(metricName, key -> {
                        ConcurrentHashMap<String, List<Mo>> tempMap = new ConcurrentHashMap<>();
                        tempMap.put(category, new ArrayList<>());
                        return tempMap;
                    });
                    moMap.get(metricName).computeIfAbsent(category, key -> new ArrayList<>());
                    moMap.get(metricName).get(category).add(mo);
                    return Mono.just(mo);
                }))
                .zipWhen(mo -> moRepository.save(mo))
                .map(tuple2 -> metric);
    }
}
