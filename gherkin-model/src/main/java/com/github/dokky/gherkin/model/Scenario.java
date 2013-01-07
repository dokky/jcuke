package com.github.dokky.gherkin.model;


import lombok.Data;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;

@Data
public class Scenario {
    private final @NonNull String name;
    private final List<Tag>        tags  = new LinkedList<Tag>();
    private final LinkedList<Step> steps = new LinkedList<Step>();

    public Step getLastStep() {
        return steps.getLast();
    }
}
