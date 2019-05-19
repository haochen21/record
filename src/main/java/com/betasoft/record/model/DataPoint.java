package com.betasoft.record.model;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * 这个比较消耗空间，但可以提供更多的灵活性，比如存储一些信息
     * 或者这个指标存储是字符串
     * 从业务上，没有通过tag查询的需求，所以不做索引
     */
    @Column("tags")
    private Map<String, String> tags = new HashMap<>();

}
