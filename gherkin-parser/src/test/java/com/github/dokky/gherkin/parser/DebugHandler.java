package com.github.dokky.gherkin.parser;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DebugHandler implements GherkinParserHandler {

    private final StringWriter result = new StringWriter();
    private final PrintWriter  out    = new PrintWriter(result);

    public String getResult() {
        return result.toString();
    }

    private String decorate(String description) {
        return description != null ? "[" + description + "]": "";
    }

    @Override
    public void onFeature(String name, String description) {
        out.print(decorate("Feature:"));
        out.print(decorate(name));
        out.print(decorate(description));
    }

    @Override
    public void onBackground(String name) {
        out.print(decorate("Background:"));
        out.print(decorate(name));
    }

    @Override
    public void onScenario(String name) {
        out.print(decorate("Scenario:"));
        out.print(decorate(name));
    }

    @Override
    public void onScenarioOutline(String name) {
        out.print(decorate("Scenario Outline:"));
        out.print(decorate(name));
    }

    @Override
    public void onExamples(String name) {
        out.print(decorate("Examples:"));
        out.print(decorate(name));
    }

    @Override
    public void onStep(String stepType, String name) {
        out.print(decorate(stepType));
        out.print(decorate(name));
    }

    @Override
    public void onTableRow(String[] cells) {
        for (String cell : cells) {
            out.print(decorate("|"));
            out.print(decorate(cell));
        }
        out.print(decorate("|"));
    }

    @Override
    public void onTag(String name) {
        out.print(decorate(name));
    }

    @Override
    public void onPyString(String pyString) {
        out.print(decorate("\"\"\""));
        out.print(decorate(pyString));
        out.print(decorate("\"\"\""));
    }

    @Override
    public void onComment(String comment, boolean hasNewLineBefore) {
        out.print(decorate(comment));
    }

    @Override
    public void onText(String text) {
        out.print(decorate(text));
    }

    @Override
    public void onWhitespaces(String whitespaces) {
        out.print(decorate(whitespaces.replace(' ', '_').replaceAll("\t", "\\\\t").replaceAll("\n", "\\\\n")));
        if (whitespaces.contains("\n")) {
            out.println();
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {
       out.flush();
    }
}
