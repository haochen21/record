package com.betasoft.record.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.cassandra.core.mapping.*;

import java.util.Set;
import java.util.TreeSet;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(value = "metric_tag")
public class MetricTag {

    @PrimaryKey
    private MetricTagKey key;

    @Column("tag_values")
    @Indexed("tag_values_idx")
    private Set<String> tagValues = new TreeSet<>();
}
