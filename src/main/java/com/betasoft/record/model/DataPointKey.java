package com.betasoft.record.model;

import lombok.*;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.util.Date;

/**
 * 指标+管理对象+日期可以保证插入数据时，不会有热点区域，写操作和读操作可以平均分布在节点中
 * 管理对象相当于bucket
 */
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@PrimaryKeyClass
public class DataPointKey implements Serializable {

    @PrimaryKeyColumn(name = "metric", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String metric;

    @PrimaryKeyColumn(name = "mo_type", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String moType;

    @PrimaryKeyColumn(name = "mo_id", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    private String moId;

    // yyyy-MM-dd
    @PrimaryKeyColumn(name = "day", ordinal = 3, type = PrimaryKeyType.PARTITIONED)
    private String day;

    // Timestamps are always stored converted to GMT,
    // and then it's your application responsibility to convert it into correct representation
    // DESC: 新的时间保存在前面
    @PrimaryKeyColumn(name = "event_time", ordinal = 4, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Date eventTime;
}
