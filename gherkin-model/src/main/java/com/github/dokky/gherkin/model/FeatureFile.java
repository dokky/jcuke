package com.github.dokky.gherkin.model;

import lombok.Data;

@Data
public class FeatureFile {
    private Feature feature;

    public void validate() {
        if (feature == null) {
            throw new GherkinParseException("'Feature' is not defined");
        }
        for (Scenario scenario : feature.getScenarios()) {
            if (scenario instanceof ScenarioOutline) {
                ScenarioOutline scenarioOutline = (ScenarioOutline) scenario;
                Examples examples = scenarioOutline.getExamples();
                if (examples == null || examples.getTable() == null || examples.getTable().getRows().isEmpty()) {
                    throw new GherkinParseException("'Scenario Outline: " + scenarioOutline.getName() + "' does not has proper 'Examples:' section");
                }
            }
        }
    }
}
