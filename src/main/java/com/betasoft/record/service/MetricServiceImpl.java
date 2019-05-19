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
        });

    }

    @Override
    public Flux<DataPointWithTtl> saveMetrics(Flux<Metric> metricFlux) {
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

        return filterMetricFlux.doOnNext(metric -> {
            String metricName = metric.getName();
            String category = metric.getTags().get("moc");
            String moPath = metric.getTags().get("mo");

            findByMetricAndMo(metricName, category, moPath)
                    .doOnSuccess(mo -> {
                        if (mo == null) {
                            MoKey metricMoKey = new MoKey(metricName, category);
                            mo = new Mo();
                            mo.setMoKey(metricMoKey);
                            mo.setMoPath(moPath);
                            for (Map.Entry<String, String> entry : metric.getTags().entrySet()) {
                                if (!entry.getKey().equals("mo") &&
                                        !entry.getKey().equals("category")) {
                                    mo.getTags().put(entry.getKey(), entry.getValue());
                                }
                            }
                            moRepository.save(mo).subscribe();
                            moMap.computeIfAbsent(metricName,key -> {
                                ConcurrentHashMap<String, List<Mo>> tempMap = new ConcurrentHashMap<>();
                                tempMap.put(category,new ArrayList<>());
                                return tempMap;
                            });
                            moMap.get(metricName).computeIfAbsent(category,key -> new ArrayList<>());
                            moMap.get(metricName).get(category).add(mo);
                        }
                    }).subscribe();
        }).flatMap(metric -> {
            String metricName = metric.getName();
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

                        DataPointWithTtl dataPointWithTtl = new DataPointWithTtl(dataPoint, ttl);
                        return dataPointWithTtl;
                    });
        }).doOnNext(dataPointWithTtl -> {
            dataPointRepository.insert(dataPointWithTtl.getDataPoint(), dataPointWithTtl.getTtl()).subscribe();
        });

    }

    public Mono<Mo> findByMetricAndMo(String metric, String category, String moPath) {
        Optional<Mo> moOptional = moMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(metric))
                .map(entry -> entry.getValue())
                .flatMap(entry1 -> entry1.entrySet().stream())
                .filter(entry -> entry.getKey().equals(category))
                .map(entry -> entry.getValue())
                .flatMap(List::stream)
                .filter(mo -> mo.getMoPath().equals(moPath))
                .findAny();
        return Mono.justOrEmpty(moOptional);
    }
}
