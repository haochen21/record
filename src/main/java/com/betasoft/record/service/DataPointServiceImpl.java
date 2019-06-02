package com.betasoft.record.service;

import com.betasoft.record.builder.*;
import com.betasoft.record.model.DataPoint;
import com.betasoft.record.repository.DataPointRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.*;

@Service
public class DataPointServiceImpl implements DataPointService {

    @Autowired
    DataPointRepository dataPointRepository;

    @Autowired
    Scheduler scheduler;

    private static final Logger logger = LoggerFactory.getLogger(DataPointServiceImpl.class);

    @Override
    public Mono<Queries> query(Mono<QueryBuilder> queryBuilder) {

        return queryBuilder.flatMap(builder -> {
            Date beginDate = new Date(builder.getBeginDate());
            Date endDate = new Date(builder.getEndDate());
            QueryMetric queryMetric = builder.getMetrics().get(0);
            String metric = queryMetric.getName();

            // 每一个tag生成一个集合
            Set<String> keys = queryMetric.getTags().keySet();
            // 让到值最多的一个集合
            Iterator<String> keyIte = keys.iterator();
            String maxSizeKey = keyIte.next();
            while (keyIte.hasNext()) {
                String key = keyIte.next();
                if (queryMetric.getTags().get(maxSizeKey).size() <
                        queryMetric.getTags().get(key).size()) {
                    maxSizeKey = key;
                }
            }
            List<Map<String, String>> tagMaps = new ArrayList<>();
            for (String tagValue : queryMetric.getTags().get(maxSizeKey)) {
                Map<String, String> tag = new TreeMap<>();
                tag.put(maxSizeKey, tagValue);
                tagMaps.add(tag);
            }
            for (String key : keys) {
                if (!key.equals(maxSizeKey)) {
                    for (String tagValue : queryMetric.getTags().get(key)) {
                        for (Map<String, String> map : tagMaps) {
                            map.put(key, tagValue);
                        }
                    }
                }
            }
            if (queryMetric.getAggregators() == null) {
                return find(metric, tagMaps, beginDate, endDate);
            } else {
                QueryAggregator queryAggregator = queryMetric.getAggregators().get(0);
                String aggregatorName = queryAggregator.getName();
                if (aggregatorName.equals("avg")) {
                    return avg(metric, tagMaps, beginDate, endDate);
                } else if (aggregatorName.equals("max")) {
                    return max(metric, tagMaps, beginDate, endDate);
                } else if (aggregatorName.equals("min")) {
                    return min(metric, tagMaps, beginDate, endDate);
                }
            }
            return Mono.empty();
        }).subscribeOn(scheduler);

    }

    @Override
    public Mono<Queries> find(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, tagMaps);
        List<String> tagJsons = getTagJson(tagMaps);
        Mono<List<DataPoint>> dataPointsMono = dataPointRepository.findSamplePoints(metric, tagJsons, beginDate, endDate)
                .collectList();
        return dataPointsMono.map(dataPoints -> {
            dataPoints.forEach(dataPoint -> {
                Object[] samplePoint = {dataPoint.getDataPointKey().getEventTime().getTime(), dataPoint.getValue()};
                resultMap.get(dataPoint.getDataPointKey().getTagJson()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        }).subscribeOn(scheduler);
    }

    @Override
    public Mono<Queries> avg(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, tagMaps);
        List<String> tagJsons = getTagJson(tagMaps);
        Mono<List<AggregatorPoint>> aggregatorPointsMono = dataPointRepository.avg(metric, tagJsons, beginDate, endDate);
        return aggregatorPointsMono.map(aggregatorPoints -> {
            aggregatorPoints.forEach(aggregatorPoint -> {
                Object[] samplePoint = {new Date().getTime(), aggregatorPoint.getValue()};
                resultMap.get(aggregatorPoint.getTagJson()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        }).subscribeOn(scheduler);
    }

    @Override
    public Mono<Queries> max(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, tagMaps);
        List<String> tagJsons = getTagJson(tagMaps);
        Mono<List<AggregatorPoint>> aggregatorPointsMono = dataPointRepository.max(metric, tagJsons, beginDate, endDate);
        return aggregatorPointsMono.map(aggregatorPoints -> {
            aggregatorPoints.forEach(aggregatorPoint -> {
                Object[] samplePoint = {new Date().getTime(), aggregatorPoint.getValue()};
                resultMap.get(aggregatorPoint.getTagJson()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        }).subscribeOn(scheduler);
    }

    @Override
    public Mono<Queries> min(String metric, List<Map<String, String>> tagMaps, Date beginDate, Date endDate) {
        Map<String, Result> resultMap = createResultMap(metric, tagMaps);
        List<String> tagJsons = getTagJson(tagMaps);
        Mono<List<AggregatorPoint>> aggregatorPointsMono = dataPointRepository.min(metric, tagJsons, beginDate, endDate);
        return aggregatorPointsMono.map(aggregatorPoints -> {
            aggregatorPoints.forEach(aggregatorPoint -> {
                Object[] samplePoint = {new Date().getTime(), aggregatorPoint.getValue()};
                resultMap.get(aggregatorPoint.getTagJson()).getSamplePoints().add(samplePoint);
            });
            Query query = new Query(new ArrayList<>(resultMap.values()));
            Queries queries = new Queries(Arrays.asList(query));
            return queries;
        }).subscribeOn(scheduler);
    }

    private Map<String, Result> createResultMap(String metric, List<Map<String, String>> tags) {
        Map<String, Result> resultMap = new HashMap<>();
        for (Map<String, String> tag : tags) {
            Result result = new Result();
            result.setName(metric);

            Map<String, List<String>> resultTags = new HashMap<>();
            tag.forEach((key, value) -> resultTags.put(key, Arrays.asList(value)));
            result.setTags(resultTags);

            List<TagGroupResult> tagGroupResults = new ArrayList<>();
            tag.forEach((key, value) -> {
                TagGroupResult tagGroupResult = new TagGroupResult(key);
                tagGroupResult.setTags(Arrays.asList(value));
                tagGroupResults.add(tagGroupResult);
            });

            result.setTagGroupResults(tagGroupResults);

            List<Object[]> samplePoints = new ArrayList<>();
            result.setSamplePoints(samplePoints);

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String tagJson = objectMapper.writeValueAsString(tag);
                resultMap.put(tagJson, result);
            } catch (Exception ex) {
                logger.error("", ex);
            }
        }
        return resultMap;
    }

    private List<String> getTagJson(List<Map<String, String>> tagMaps) {
        List<String> tagJsons = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        for (Map<String, String> map : tagMaps) {
            try {
                String tagJson = objectMapper.writeValueAsString(map);
                tagJsons.add(tagJson);
            } catch (Exception ex) {
                logger.error("", ex);
            }
        }
        return tagJsons;
    }
}
