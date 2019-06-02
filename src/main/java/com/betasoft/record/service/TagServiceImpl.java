package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.MetricTag;
import com.betasoft.record.repository.MetricTagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class TagServiceImpl implements TagService {

    @Autowired
    MetricTagRepository metricTagRepository;

    //metric:tagKey:tagValues
    ConcurrentHashMap<String, ConcurrentHashMap<String, Set<String>>> tagMap;

    private static final Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    public TagServiceImpl() {

    }

    @PostConstruct
    public void init() {
        tagMap = new ConcurrentHashMap<>();
        metricTagRepository.findAll()
                .subscribe(tag -> tagMap.computeIfAbsent(tag.getKey().getMetric(), key -> new ConcurrentHashMap<>())
                        .computeIfAbsent(tag.getKey().getTagKey(), key -> new TreeSet<>())
                        .addAll(tag.getTagValues())
                );
    }

    @Override
    public Mono<Metric> saveMetricTag(Metric metric) {
        return Mono.just(metric)
                .map(m -> {
                    List<String[]> tags = new ArrayList<>();
                    m.getTags().forEach((tagKey, tagValue) -> {
                        tags.add(new String[]{tagKey, tagValue});
                    });
                    return tags;
                })
                .flatMap(tags -> Flux.fromIterable(tags)
                        .flatMap(tag -> {
                            if (!tagMap.containsKey(metric.getName())
                                    || !tagMap.get(metric.getName()).containsKey(tag[0])) {

                                tagMap.computeIfAbsent(metric.getName(), key -> new ConcurrentHashMap<>())
                                        .computeIfAbsent(tag[0], key -> new TreeSet<>())
                                        .add(tag[1]);

                                return metricTagRepository.create(metric.getName(), tag[0], tag[1]);
                            } else if (!tagMap.get(metric.getName()).get(tag[0]).contains(tag[1])) {

                                tagMap.computeIfAbsent(metric.getName(), key -> new ConcurrentHashMap<>())
                                        .computeIfAbsent(tag[0], key -> new TreeSet<>())
                                        .add(tag[1]);

                                return metricTagRepository.addTagValue(metric.getName(), tag[0], tag[1]);
                            } else {
                                return Mono.just(true);
                            }
                        })
                        .count())
                .map(count -> metric);
    }

    @Override
    public Mono<List<String>> filterMetric(Mono<Map> metricMono) {
        return metricMono.zipWhen(metricMap -> Flux.fromStream(tagMap.entrySet().stream())
                .map(entry -> entry.getKey())
                .filter(tagKey -> tagKey.toLowerCase().contains(metricMap.get("metric").toString().toLowerCase()))
                .collectList())
                .map(tuple2 -> tuple2.getT2());
    }

    @Override
    public Mono<List<String>> findTagKeyByMetric(Mono<Map> metricMono) {
        return metricMono.zipWhen(metricMap ->
                Flux.fromArray(tagMap.get(metricMap.get("metric").toString()).keySet().toArray(new String[0]))
                        .collectList())
                .map(tuple2 -> tuple2.getT2());
    }

    @Override
    public Mono<List<String>> findTagValueByMetricAndTagKey(Mono<Map> metricMono) {
        return metricMono.zipWhen(metricMap ->
                Flux.fromIterable(tagMap.get(metricMap.get("metric").toString()).get(metricMap.get("tagKey").toString()))
                        .collectList())
                .map(tuple2 -> tuple2.getT2());
    }
}
