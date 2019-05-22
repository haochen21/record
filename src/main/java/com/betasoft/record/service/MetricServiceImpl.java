package com.betasoft.record.service;

import com.betasoft.record.builder.Metric;
import com.betasoft.record.model.*;
import com.betasoft.record.repository.DataPointRepository;
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
    MoServiceImpl moServiceImpl;

    @Autowired
    Scheduler scheduler;

    public MetricServiceImpl() {

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
                .flatMap(metric -> moServiceImpl.saveMo(metric).map(metric1 -> metric1))
                .flatMap(this::getDataPoint)
                .flatMap(dataPointWrapper ->
                        dataPointRepository.insert(dataPointWrapper.getDataPoint(), dataPointWrapper.getTtl()))
                .count();

    }


    private Flux<DataPointWrapper> getDataPoint(Metric metric) {
        String metricName = metric.getName();
        String moType = metric.getTags().get("moc");
        String moPath = metric.getTags().get("mo");
        int ttl = metric.getTtl();

        return Flux.fromIterable(metric.getSamplePoints())
                .map(samplePoint -> {
                    Date eventTime = new Date((Long) samplePoint[0]);
                    Double value = Double.parseDouble(samplePoint[1].toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                    DataPointKey dataPointKey = new DataPointKey(metricName, moType, moPath, sdf.format(eventTime), eventTime);
                    metric.getTags().remove("moc");
                    metric.getTags().remove("mo");
                    DataPoint dataPoint = new DataPoint(dataPointKey, value, metric.getTags());
                    dataPoint.setDataPointKey(dataPointKey);

                    DataPointWrapper dataPointWrapper = new DataPointWrapper(dataPoint, ttl, metricName, moType, moPath);
                    return dataPointWrapper;
                }).subscribeOn(scheduler);
    }


}
