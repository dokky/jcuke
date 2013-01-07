package com.github.dokky.gherkin.parser;

public interface FeatureHandler {
    public void onFeature(String name, String description);

    public void onBackground(String name);

    public void onScenario(String name);

    public void onScenarioOutline(String name);

    public void onExamples(String name);

    public void onStep(String stepType, String name);

    public void onTableRow(String[] cells);

    public void onTag(String name);

    public void onPyString(String pyString);

    public void onComment(String comment);

    public void onText(String text);

    public void onWhitespaces(String whitespaces);
}