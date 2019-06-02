package com.betasoft.record.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import reactor.core.publisher.Mono;

public class CustomizeMetricTagRepositoryImpl implements CustomizeMetricTagRepository {

    @Autowired
    private ReactiveCassandraTemplate template;

    @Override
    public Mono<Boolean> create(String metricName, String tagKey, String tagValue) {
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("INSERT INTO metric_tag (metric,tag_key,tag_values) ");
        sqlSb.append("VALUES ('").append(metricName).append("',");
        sqlSb.append("'").append(tagKey).append("',");
        sqlSb.append("{'").append(tagValue).append("'} )");
        return Mono.just(sqlSb.toString())
                .flatMap(sql -> template.getReactiveCqlOperations().execute(sql));
    }

    @Override
    public Mono<Boolean> addTagValue(String metricName, String tagKey, String tagValue) {
        StringBuilder sqlSb = new StringBuilder();
        sqlSb.append("UPDATE metric_tag SET tag_values = tag_values + {'");
        sqlSb.append(tagValue).append("'} ");
        sqlSb.append("WHERE metric = '").append(metricName).append("' ");
        sqlSb.append(" AND tag_key = '").append(tagKey).append("'");
        return Mono.just(sqlSb.toString())
                .flatMap(sql -> template.getReactiveCqlOperations().execute(sql));
    }
}
