package com.betasoft.record.service;

import com.betasoft.record.builder.*;
import com.betasoft.record.model.DataPoint;
import com.betasoft.record.repository.DataPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.util.*;

public class DataPointServiceImpl implements DataPointService {

    @Autowired
    DataPointRepository dataPointRepository;

    @Override
    public Mono<Queries> query(QueryBuilder queryBuilder) {
        Date beginDate = new Date(queryBuilder.getBeginDate());
        Date endDate = new Date(queryBuilder.getEndDate());
        QueryMetric queryMetric = queryBuilder.getMetrics().get(0);
        String metric = queryMetric.getName();
        String moc = queryMetric.getTags().get("moc").iterator().next();
        List<String> mos = new ArrayList<>(queryMetric.getTags().get("mo"));
        if (queryMetric.getAggregators() == null) {
            return find(metric, moc, mos, beginDate, endDate);
        } else {
            QueryAggregator queryAggregator = queryMetric.getAggregators().get(0);
            String aggregatorName = queryAggregator.getName();
            if (aggregatorName.equals("avg")) {
                return avg(metric, moc, mos, beginDate, endDate);
            } else if (aggregatorName.equals("max")) {
                return max(metric, moc, mos, beginDate, endDate);
            } else if (aggregatorName.equals("min")) {
                return min(metric, moc, mos, beginDate, endDate);
            }
        }
        return Mono.empty();
    }

    @Override
    public Mono<Queries> find(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, moc, mos);

        Mono<List<DataPoint>> dataPointsMono = dataPointRepository.findSamplePoints(metric, moc, mos, beginDate, endDate)
                .collectList();
        return dataPointsMono.map(dataPoints -> {
            dataPoints.forEach(dataPoint -> {
                Object[] samplePoint = {dataPoint.getDataPointKey().getEventTime().getTime(), dataPoint.getValue()};
                resultMap.get(dataPoint.getDataPointKey().getMo()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        });
    }

    @Override
    public Mono<Queries> avg(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, moc, mos);
        Mono<List<AggregatorPoint>> aggregatorPointsMono = dataPointRepository.avg(metric, moc, mos, beginDate, endDate)
                .collectList();
        return aggregatorPointsMono.map(aggregatorPoints -> {
            aggregatorPoints.forEach(aggregatorPoint -> {
                Object[] samplePoint = {new Date().getTime(), aggregatorPoint.getValue()};
                resultMap.get(aggregatorPoint.getMo()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        });
    }

    @Override
    public Mono<Queries> max(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, moc, mos);
        Mono<List<AggregatorPoint>> aggregatorPointsMono = dataPointRepository.max(metric, moc, mos, beginDate, endDate)
                .collectList();
        return aggregatorPointsMono.map(aggregatorPoints -> {
            aggregatorPoints.forEach(aggregatorPoint -> {
                Object[] samplePoint = {new Date().getTime(), aggregatorPoint.getValue()};
                resultMap.get(aggregatorPoint.getMo()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        });
    }

    @Override
    public Mono<Queries> min(String metric, String moc, List<String> mos, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, moc, mos);
        Mono<List<AggregatorPoint>> aggregatorPointsMono = dataPointRepository.min(metric, moc, mos, beginDate, endDate)
                .collectList();
        return aggregatorPointsMono.map(aggregatorPoints -> {
            aggregatorPoints.forEach(aggregatorPoint -> {
                Object[] samplePoint = {new Date().getTime(), aggregatorPoint.getValue()};
                resultMap.get(aggregatorPoint.getMo()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        });
    }

    private Map<String, Result> createResultMap(String metric, String moc, List<String> mos) {
        Map<String, Result> resultMap = new HashMap<>();
        for (String mo : mos) {
            Result result = new Result();
            result.setName(metric);

            Map<String, List<String>> tags = new HashMap<>();
            tags.put("moc", Arrays.asList(moc));
            tags.put("mo", Arrays.asList(mo));
            result.setTags(tags);

            TagGroupResult tagGroupResult = new TagGroupResult(mo);
            List<TagGroupResult> tagGroupResults = new ArrayList<>(Arrays.asList(tagGroupResult));
            result.setTagGroupResults(tagGroupResults);

            List<Object[]> samplePoints = new ArrayList<>();
            result.setSamplePoints(samplePoints);

            resultMap.put(mo, result);
        }
        return resultMap;
    }
}
