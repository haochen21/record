package com.betasoft.record.repository;

import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import org.springframework.data.cassandra.repository.query.CassandraEntityInformation;
import org.springframework.data.cassandra.repository.support.SimpleReactiveCassandraRepository;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

public class ExtendedReactiveCassandraRepositoryImpl<T, ID> extends SimpleReactiveCassandraRepository<T, ID>
        implements ExtendedReactiveCassandraRepository<T, ID> {

    private static final InsertOptions INSERT_NULLS = InsertOptions.builder().withInsertNulls().build();

    private final ReactiveCassandraOperations operations;

    public ExtendedReactiveCassandraRepositoryImpl(CassandraEntityInformation<T, ID> metadata, ReactiveCassandraOperations operations) {
        super(metadata, operations);
        this.operations = operations;
    }

    @Override
    public <S extends T> Mono<S> insert(S entity, int ttl) {
        Assert.notNull(entity, "Entity must not be null");
        if(ttl>0){
            InsertOptions insertOptions = InsertOptions.builder().ttl(ttl).build();
            return operations.insert(entity, insertOptions).thenReturn(entity);
        }else {
            return operations.insert(entity, INSERT_NULLS).thenReturn(entity);
        }
    }
}
