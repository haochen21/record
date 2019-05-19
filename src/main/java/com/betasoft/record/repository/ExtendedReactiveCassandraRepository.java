package com.betasoft.record.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface ExtendedReactiveCassandraRepository <T, ID>
        extends ReactiveCassandraRepository<T, ID> {

    <S extends T> Mono<S> insert(S entity, int ttl);
}
