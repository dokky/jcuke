package com.github.dokky.gherkin.parser;

import com.github.dokky.gherkin.lexer.Lexer;

public class FeaturePrettyFormatter implements FeatureHandler {
    private final static int    DEFAULT_BUFFER_SIZE = 350 * 1024;
    private final static String IDENT               = "    ";
    private StringBuilder out;


    public String getResult() {
        return out.toString();
    }

    @Override
    public void start() {
        out = new StringBuilder(DEFAULT_BUFFER_SIZE);
    }

    @Override
    public void onFeature(String name, String description) {
        out.append('\n');
        out.append("Feature: ");
        out.append(name);
        out.append('\n');
        if (description != null) {
            out.append(IDENT);
            out.append(description);
            out.append('\n');
        }
    }

    @Override
    public void onBackground(String name) {
        out.append('\n');
        out.append(IDENT);
        out.append("Background: ");
        if (name != null) {
            out.append(name);
        }
        out.append('\n');
    }

    @Override
    public void onScenario(String name) {
        out.append('\n');
        out.append(IDENT);
        out.append("Scenario: ");
        out.append(name);
        out.append('\n');
    }

    @Override
    public void onScenarioOutline(String name) {
        out.append('\n');
        out.append(IDENT);
        out.append("Scenario Outline: ");
        out.append(name);
        out.append('\n');
    }

    @Override
    public void onExamples(String name) {
        out.append('\n');
        out.append(IDENT);
        out.append("Examples: ");
        if (name != null) {
            out.append(name);
        }
        out.append('\n');
    }

    @Override
    public void onStep(String stepType, String name) {
        out.append(IDENT);
        out.append(IDENT);
        out.append(stepType);
        out.append(' ');
        out.append(name);
        out.append('\n');
    }

    @Override
    public void onTableRow(String[] cells) {
        out.append(IDENT);
        out.append(IDENT);
        for (String cell : cells) {
            out.append("\t| ");
            out.append(cell);
        }
        out.append("\t|");
        out.append('\n');
    }

    @Override
    public void onTag(String name) {
        out.append(name);
        out.append(' ');
    }

    @Override
    public void onPyString(String pyString) {
        if (pyString.startsWith(Lexer.PYSTRING)) {
            pyString = pyString.substring(3);
        }
        if (pyString.endsWith(Lexer.PYSTRING)) {
            pyString = pyString.substring(0, pyString.length() - 3).trim();
        }
        out.append(IDENT);
        out.append(Lexer.PYSTRING);
        out.append('\n');
        out.append(pyString);
        out.append('\n');
        out.append(IDENT);
        out.append(Lexer.PYSTRING);
        out.append('\n');
    }

    @Override
    public void onComment(String comment) {
        out.append(comment);
        out.append('\n');
    }

    @Override
    public void onText(String text) {
        out.append(text);
        out.append('\n');
    }

    @Override
    public void onWhitespaces(String whitespaces) {
//        out.append(whitespaces);
    }

    @Override
    public void end() {

    }

}
