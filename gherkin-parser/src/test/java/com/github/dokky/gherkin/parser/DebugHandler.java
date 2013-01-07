package com.github.dokky.gherkin.parser;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DebugHandler implements FeatureHandler {

    private final StringWriter result = new StringWriter();
    private final PrintWriter  out    = new PrintWriter(result);

    public String getResult() {
        return result.toString();
    }

    private String decorate(String description) {return "[" + description + "]";}

    @Override
    public void onFeature(String name, String description) {
        out.print(decorate("Feature:"));
        out.println(decorate(name));
        out.println(decorate(description));
    }

    @Override
    public void onBackground(String name) {
        out.print(decorate("Background:"));
        out.println(decorate(name));
    }

    @Override
    public void onScenario(String name) {
        out.print(decorate("Scenario:"));
        out.println(decorate(name));
    }

    @Override
    public void onScenarioOutline(String name) {
        out.print(decorate("Scenario Outline:"));
        out.println(decorate(name));
    }

    @Override
    public void onExamples(String name) {
        out.print(decorate("Examples:"));
        out.println(decorate(name));
    }

    @Override
    public void onStep(String stepType, String name) {
        out.print(decorate(stepType));
        out.println(decorate(name));
    }

    @Override
    public void onTableRow(String[] cells) {
        for (String cell : cells) {
            out.print(decorate("|"));
            out.print(decorate(cell));
        }
        out.println(decorate("|"));
    }

    @Override
    public void onTag(String name) {
        out.print(decorate(name));
    }

    @Override
    public void onPyString(String pyString) {
        out.print(decorate("\"\"\""));
        out.print(decorate(pyString));
        out.println(decorate("\"\"\""));
    }

    @Override
    public void onComment(String comment) {
        out.print(decorate(comment));
    }

    @Override
    public void onText(String text) {
        out.print(decorate(text));
    }

    @Override
    public void onWhitespaces(String whitespaces) {
        out.print(decorate(whitespaces));
    }
}
