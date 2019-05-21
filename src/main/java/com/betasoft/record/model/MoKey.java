package com.betasoft.record.model;

import lombok.*;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@PrimaryKeyClass
public class MoKey implements Serializable {

    @PrimaryKeyColumn(name = "metric", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String metric;

    @PrimaryKeyColumn(name = "mo_type", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String moType;

    @PrimaryKeyColumn(name = "mo_path", ordinal = 3, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private String moPath;
}
