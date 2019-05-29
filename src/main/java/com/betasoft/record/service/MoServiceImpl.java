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
                                !entry.getKey().equals("moc")) {
                            mo.getTags().put(entry.getKey(), entry.getValue());
                        }
                    }
                    moMap.computeIfAbsent(metricName, key -> new ConcurrentHashMap<>())
                            .computeIfAbsent(category, key -> new ArrayList<>())
                            .add(mo);
                    return Mono.just(mo);
                }))
                .zipWhen(mo -> moRepository.save(mo))
                .map(tuple2 -> metric);
    }

    @Override
    public Mono<List<String>> filterMetric(Mono<Map> metricMono) {
        return metricMono.zipWhen(metricMap -> Flux.fromStream(moMap.entrySet().stream())
                .map(entry -> entry.getKey())
                .filter(metricKey -> metricKey.toLowerCase().contains(metricMap.get("metric").toString().toLowerCase()))
                .collectList())
                .map(tuple2 -> tuple2.getT2());
    }

    @Override
    public Mono<List<String>> findMoTypeByMetric(Mono<Map> metricMono) {
        return metricMono.zipWhen(metricMap ->
                Flux.fromArray(moMap.get(metricMap.get("metric").toString()).keySet().toArray(new String[0]))
                        .collectList())
                .map(tuple2 -> tuple2.getT2());
    }

    @Override
    public Mono<List<String>> findMoByMetricAndMoType(Mono<Map> metricMono) {
        return metricMono.zipWhen(metricMap ->
                Flux.fromIterable(moMap.get(metricMap.get("metric").toString()).get(metricMap.get("moType").toString()))
                        .map(mo -> mo.getMoKey().getMoId())
                        .collectList())
                .map(tuple2 -> tuple2.getT2());
    }
}
