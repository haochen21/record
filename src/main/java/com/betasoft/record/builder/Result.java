package com.betasoft.record.builder;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Result {

    private String name;

    private List<Object[]> samplePoints;

    private List<TagGroupResult> tagGroupResults;

    private Map<String,List<String>> tags;

    public Result(){

    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("values")
    public List<Object[]> getSamplePoints() {
        return samplePoints;
    }

    public void setSamplePoints(List<Object[]> samplePoints) {
        this.samplePoints = samplePoints;
    }

    @JsonProperty("group_by")
    public List<TagGroupResult> getTagGroupResults() {
        return tagGroupResults;
    }

    public void setTagGroupResults(List<TagGroupResult> tagGroupResults) {
        this.tagGroupResults = tagGroupResults;
    }

    @JsonProperty("tags")
    public Map<String, List<String>> getTags() {
        return tags;
    }

    public void setTags(Map<String, List<String>> tags) {
        this.tags = tags;
    }
}
