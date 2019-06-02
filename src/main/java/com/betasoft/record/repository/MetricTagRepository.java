package com.betasoft.record.repository;

import com.betasoft.record.model.MetricTag;
import com.betasoft.record.model.MetricTagKey;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricTagRepository
        extends ExtendedReactiveCassandraRepository<MetricTag, MetricTagKey>, CustomizeMetricTagRepository {
}
