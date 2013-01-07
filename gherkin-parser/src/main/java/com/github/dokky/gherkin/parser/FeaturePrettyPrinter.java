package com.github.dokky.gherkin.parser;

public class FeaturePrettyPrinter implements FeatureHandler {
    @Override
    public void onFeature(String name, String description) {
        System.out.println("[Feature: ][" + name + "]");
        System.out.println("[" + description + "]");
    }

    @Override
    public void onBackground(String name) {
        System.out.println("[Background: ][" + name + "]");
    }

    @Override
    public void onScenario(String name) {
        System.out.println("[Scenario: ][" + name + "]");
    }

    @Override
    public void onScenarioOutline(String name) {
        System.out.println("[Scenario Outline: ][" + name + "]");
    }

    @Override
    public void onExamples(String name) {
        System.out.println("[Examples: ][" + name + "]");
    }

    @Override
    public void onStep(String stepType, String name) {
        System.out.println("[" + stepType + "] [" + name + "]");
    }

    @Override
    public void onTableRow(String[] cells) {
        for (String cell : cells) {
            System.out.print("[|][" + cell + "]");
        }
        System.out.println("[|]");
    }

    @Override
    public void onTag(String name) {
        System.out.print("[" + name + "]");
    }

    @Override
    public void onPyString(String pyString) {
        System.out.println("[\"\"\"][" + pyString + "][\"\"\"]");
    }

    @Override
    public void onComment(String comment) {
        System.out.print("[" + comment + "]");
    }

    @Override
    public void onText(String text) {
        System.out.print("[" + text + "]");
    }

    @Override
    public void onWhitespaces(String whitespaces) {
        System.out.print("[" + whitespaces + "]");
    }
}
