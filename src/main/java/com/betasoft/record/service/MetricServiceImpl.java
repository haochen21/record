package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.*;
import com.betasoft.record.repository.DataPointRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MetricServiceImpl implements MetricService {

    @Autowired
    DataPointRepository dataPointRepository;

    @Autowired
    TagServiceImpl tagServiceImpl;

    @Autowired
    Scheduler scheduler;

    private static final Logger logger = LoggerFactory.getLogger(MetricServiceImpl.class);

    public MetricServiceImpl() {

    }

    @Override
    public Mono<Long> saveMetrics(Flux<Metric> metricFlux) {
        Flux<Metric> filterMetricFlux = metricFlux.filter(metric -> {
            if (metric.getTags() == null || metric.getTags().size() == 0) {
                return false;
            }
            // 必需包含采集的时候以及值
            if (metric.getSamplePoints() == null || metric.getSamplePoints().size() == 0) {
                return false;
            }
            return true;
        });

        return filterMetricFlux
                .flatMap(metric -> tagServiceImpl.saveMetricTag(metric).map(metric1 -> metric1))
                .flatMap(this::getDataPoint)
                .flatMap(dataPointWrapper ->
                        dataPointRepository.insert(dataPointWrapper.getDataPoint(), dataPointWrapper.getTtl()))
                .count();

    }


    private Flux<DataPointWrapper> getDataPoint(Metric metric) {
        try {
            String metricName = metric.getName();
            int ttl = metric.getTtl();
            Map<String, String> sortedTag = new TreeMap<>();
            sortedTag.putAll(metric.getTags());
            ObjectMapper mapper = new ObjectMapper();
            String tagJson = mapper.writeValueAsString(sortedTag);

            return Flux.fromIterable(metric.getSamplePoints())
                    .map(samplePoint -> {
                        Date eventTime = new Date((Long) samplePoint[0]);
                        Double value = Double.parseDouble(samplePoint[1].toString());
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                        DataPointKey dataPointKey = new DataPointKey(metricName, tagJson, sdf.format(eventTime), eventTime);
                        DataPoint dataPoint = new DataPoint(dataPointKey, value);
                        dataPoint.setDataPointKey(dataPointKey);

                        DataPointWrapper dataPointWrapper = new DataPointWrapper(dataPoint, ttl, metricName, tagJson);
                        return dataPointWrapper;
                    }).subscribeOn(scheduler);
        } catch (Exception ex) {
            logger.error("", ex);
            return Flux.empty();
        }
    }
}
