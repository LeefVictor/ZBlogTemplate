package org.springframework.cloud.gateway.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.core.style.ToStringCreator;

public class GroupWeightConfig {

    String group;

    LinkedHashMap<String, Integer> weights = new LinkedHashMap<>();

    LinkedHashMap<String, Double> normalizedWeights = new LinkedHashMap<>();

    LinkedHashMap<Integer, String> rangeIndexes = new LinkedHashMap<>();

    List<Double> ranges = new ArrayList<>();

    GroupWeightConfig(String group) {
        this.group = group;
    }

    GroupWeightConfig(GroupWeightConfig other) {
        this.group = other.group;
        this.weights = new LinkedHashMap<>(other.weights);
        this.normalizedWeights = new LinkedHashMap<>(other.normalizedWeights);
        this.rangeIndexes = new LinkedHashMap<>(other.rangeIndexes);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("group", group).append("weights", weights)
            .append("normalizedWeights", normalizedWeights).append("rangeIndexes", rangeIndexes).toString();
    }

}