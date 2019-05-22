package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.Mo;
import com.betasoft.record.model.MoKey;
import com.betasoft.record.repository.MoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class MoServiceImpl implements MoService {

    @Autowired
    MoRepository moRepository;

    //metric:moType:mos
    ConcurrentHashMap<String, ConcurrentHashMap<String, List<Mo>>> moMap;

    public MoServiceImpl() {

    }

    @PostConstruct
    public void init() {
        moRepository.findAll().collectList().subscribe(mos -> {
            moMap = mos.stream()
                    .collect(Collectors.groupingBy(mo ->
                                    mo.getMoKey().getMetric(),
                            ConcurrentHashMap::new,
                            Collectors.groupingBy(mo ->
                                            mo.getMoKey().getMoType(),
                                    ConcurrentHashMap::new,
                                    toList()
                            )
                    ));
        });
    }

    @Override
    public Mono<Mo> findByMetricAndMo(Metric metric) {
        String metricName = metric.getName();
        String moType = metric.getTags().get("moc");
        String moId = metric.getTags().get("mo");
        Optional<Mo> moOptional = moMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(metricName))
                .map(entry -> entry.getValue())
                .flatMap(entry1 -> entry1.entrySet().stream())
                .filter(entry -> entry.getKey().equals(moType))
                .map(entry -> entry.getValue())
                .flatMap(List::stream)
                .filter(mo -> mo.getMoKey().getMoId().equals(moId))
                .findAny();
        return Mono.justOrEmpty(moOptional);
    }

    @Override
    public Mono<Metric> saveMo(Metric metric) {
        return findByMetricAndMo(metric)
                .switchIfEmpty(Mono.defer(() -> {
                    String metricName = metric.getName();
                    String category = metric.getTags().get("moc");
                    String moId = metric.getTags().get("mo");
                    MoKey moKey = new MoKey(metric.getName(), category, moId);
                    Mo mo = new Mo();
                    mo.setMoKey(moKey);
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

    @Override
    public Mono<List<String>> filterMetric(Mono<Map> metricMono) {
        return metricMono.map(metricMap -> {
            String metric = metricMap.get("metric").toString();
            List<String> metrics = moMap.entrySet().stream()
                    .map(entry -> entry.getKey())
                    .filter(metricKey -> metricKey.toLowerCase().contains(metric.toLowerCase()))
                    .collect(Collectors.toList());
            return metrics;
        });
    }

    @Override
    public Mono<List<String>> findMoTypeByMetric(Mono<Map> metricMono) {
        return metricMono.map(metricMap -> {
            String metric = metricMap.get("metric").toString();
            List<String> moTypes = moMap.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(metric))
                    .map(entry -> entry.getValue())
                    .map(sortedMap -> sortedMap.keySet())
                    .flatMap(Set::stream)
                    .collect(Collectors.toList());
            return moTypes;
        });
    }

    @Override
    public Mono<List<String>> findMoByMetricAndMoType(Mono<Map> metricMono) {
        return metricMono.map(metricMap -> {
            String metric = metricMap.get("metric").toString();
            String moType = metricMap.get("moType").toString();
            List<String> mos = moMap.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(metric))
                    .map(entry -> entry.getValue())
                    .flatMap(sortedMap -> sortedMap.entrySet().stream())
                    .filter(entry -> entry.getKey().equals(moType))
                    .map(entry -> entry.getValue())
                    .flatMap(List::stream)
                    .map(Mo::getMoKey)
                    .map(MoKey::getMoId)
                    .collect(Collectors.toList());
            return mos;
        });
    }
}
