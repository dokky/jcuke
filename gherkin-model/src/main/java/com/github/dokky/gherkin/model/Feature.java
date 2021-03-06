package com.github.dokky.gherkin.model;


import lombok.Data;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;

@Data
public class Feature {

    private final @NonNull String name;
    private                String description;

    private List<Tag> tags = new LinkedList<>();
    private Background background;
    private LinkedList<Scenario> scenarios = new LinkedList<>();

    public Scenario getLastScenario() {
        return scenarios.getLast();
    }


}
