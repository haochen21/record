package com.betasoft.record.model;

import lombok.*;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@PrimaryKeyClass
public class MetricTagKey implements Serializable {

    @PrimaryKeyColumn(name = "metric", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String metric;

    @PrimaryKeyColumn(name = "tag_key", ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private String tagKey;

}
