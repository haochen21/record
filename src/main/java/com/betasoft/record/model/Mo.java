package com.betasoft.record.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(value = "mo")
public class Mo {

    @PrimaryKey
    private MoKey moKey;

    @Column("tags")
    @Indexed("mo_tags_idx")
    private Map<String, String> tags = new HashMap<>();
}
