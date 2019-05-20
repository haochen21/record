package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class TagGroupResult {

    @JsonProperty("name")
    private String name = "tag";

    @JsonProperty("tags")
    private List<String> tags = Arrays.asList(new String[]{"mo"});

    // key: "mo",value: mo
    @JsonProperty("group")
    private Map<String, String> group = new HashMap<>();

    public TagGroupResult(String mo) {
        group.put("mo",mo);
    }
}
