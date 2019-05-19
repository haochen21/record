package com.betasoft.record.repository;

import com.betasoft.record.model.DataPoint;
import com.betasoft.record.model.DataPointKey;
import org.springframework.stereotype.Repository;

@Repository
public interface DataPointRepository
        extends ExtendedReactiveCassandraRepository<DataPoint, DataPointKey>, CustomizeDataPointRepository {
}
