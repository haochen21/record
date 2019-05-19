package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagGroupResult {

    private String name = "tag";

    private List<String> tags = Arrays.asList(new String[]{"mo"});

    // key: "mo",value: mo
    private Map<String, String> group = new HashMap<>();

    public TagGroupResult(String mo) {
        group.put("mo",mo);
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }

    @JsonProperty("group")
    public Map<String, String> getGroup() {
        return group;
    }
}
