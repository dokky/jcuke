package com.github.dokky.gherkin.parser.handler;

import com.github.dokky.gherkin.model.*;
import com.github.dokky.gherkin.parser.GherkinParserHandler;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

public class GherkinModelParserHandler implements GherkinParserHandler {
    // context types
    public static final int DEFAULT = 0;
    public static final int FEATURE = 1;
    public static final int BACKGROUND = 2;
    public static final int SCENARIO = 3;
    public static final int SCENARIO_OUTLINE = 4;
    public static final int EXAMPLES = 5;

    @Getter
    private FeatureFile featureFile;
    private List<Tag> tags;
    private int context;


    @Override
    public void start() {
        featureFile = new FeatureFile();
        tags = new LinkedList<>();
        context = DEFAULT;
    }

    @Override
    public void end() {
    }

    private Feature getFeature() {
        return featureFile.getFeature();
    }

    private Step getLastStep() {
        if (context == BACKGROUND) {
            return getFeature().getBackground().getLastStep();
        } else if (context == SCENARIO || context == SCENARIO_OUTLINE) {
            return getFeature().getLastScenario().getLastStep();
        }
        return null;
    }

    public void onFeature(String name, String description) {
        if (context == DEFAULT) {
            Feature feature = new Feature(name);
            feature.setDescription(description);
            feature.setTags(tags);
            tags.clear();
            featureFile.setFeature(feature);
            context = FEATURE;
        }
    }

    public void onBackground(String name) {
        if (context == FEATURE) {
            Background background = new Background(name);
            getFeature().setBackground(background);
            context = BACKGROUND;
        }
    }

    public void onScenario(String name) {
        if (context != DEFAULT) {
            Scenario scenario = new Scenario(name);
            scenario.getTags().addAll(tags);
            tags.clear();
            getFeature().getScenarios().add(scenario);
            context = SCENARIO;
        }
    }

    public void onScenarioOutline(String name) {
        if (context != DEFAULT) {
            ScenarioOutline scenarioOutline = new ScenarioOutline(name);
            scenarioOutline.getTags().addAll(tags);
            tags.clear();
            getFeature().getScenarios().add(scenarioOutline);
            context = SCENARIO_OUTLINE;
        }
    }

    public void onExamples(String name) {
        if (context == SCENARIO_OUTLINE) {
            Examples examples = new Examples();
            examples.setName(name);
            Scenario lastScenario = getFeature().getLastScenario();
            if (lastScenario instanceof ScenarioOutline) {
                ScenarioOutline scenarioOutline = (ScenarioOutline) lastScenario;
                scenarioOutline.setExamples(examples);
            }
            context = EXAMPLES;
        }
    }


    public void onStep(String stepType, String name) {
        if (context == BACKGROUND) {
            Step step = new Step(stepType, name);
            getFeature().getBackground().getSteps().add(step);
        } else if (context == SCENARIO || context == SCENARIO_OUTLINE) {
            Step step = new Step(stepType, name);
            getFeature().getBackground().getSteps().add(step);
        }
    }

    public void onTableRow(String[] cells) {
        Table table = null;
        if (context == EXAMPLES) {
            Scenario lastScenario = getFeature().getLastScenario();
            if (lastScenario instanceof ScenarioOutline) {
                ScenarioOutline scenarioOutline = (ScenarioOutline) lastScenario;
                table = scenarioOutline.getExamples().getTable();
                if (table == null) {
                    table = new Table();
                    scenarioOutline.getExamples().setTable(table);
                }
            }
        } else {
            Step step = getLastStep();
            if (step != null) {
                table = step.getTable();
                if (table == null) {
                    table = new Table();
                    step.setTable(table);
                }
            }
        }

        if (table != null) {
            if (table.getHeadings() == null) {
                table.setHeadings(new TableRow(cells));
            } else {
                table.getRows().add(new TableRow(cells));
            }
        }
    }

    public void onTag(String tag) {
        if (context == DEFAULT || context == FEATURE) {
            tags.add(new Tag(tag));
        }
    }

    public void onPyString(String pyString) {
        Step step = getLastStep();
        if (step != null) {
            step.setPyString(new PyString(pyString));
        }
    }

    public void onComment(String comment, boolean hasNewLineBefore) {
//        log.info(comment);
    }

    public void onText(String text) {
        if (text.startsWith("Using step definitions from:")) { // freshen dialect

        } else {
            System.err.println("undefined:" + text);
        }
    }

    @Override
    public void onWhitespaces(String whitespaces) {

    }


}
