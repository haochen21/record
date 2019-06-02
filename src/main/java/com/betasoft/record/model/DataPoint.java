package com.betasoft.record.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Table(value = "data_point")
public class DataPoint {

    @PrimaryKey
    private DataPointKey dataPointKey;

    @Column("value")
    private Double value;
}
