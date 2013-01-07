package com.github.dokky.gherkin.model;


import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioOutline extends Scenario {
    private Examples examples;

    public ScenarioOutline(String name) {
        super(name);
    }
}
