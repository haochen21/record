package com.betasoft.record.repository;

import com.betasoft.record.model.Mo;
import com.betasoft.record.model.MoKey;
import org.springframework.stereotype.Repository;

@Repository
public interface MoRepository
        extends ExtendedReactiveCassandraRepository<Mo, MoKey> {
}
