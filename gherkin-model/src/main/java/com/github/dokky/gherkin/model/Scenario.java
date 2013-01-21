package com.github.dokky.gherkin.model;


import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Scenario {
    private final String name;
    private final List<Tag>        tags  = new LinkedList<>();
    private final LinkedList<Step> steps = new LinkedList<>();

    public Step getLastStep() {
        return steps.size() != 0 ? steps.getLast() : null;
    }
}
