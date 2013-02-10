package com.github.dokky.gherkin.parser;

import com.github.dokky.gherkin.lexer.Lexer;

import java.util.LinkedList;
import java.util.List;

public class FeaturePrettyFormatter implements FeatureHandler {
    private final static int    DEFAULT_BUFFER_SIZE = 350 * 1024;
    private final static String IDENT               = "    ";
    private final        char   EOL                 = '\n';

    private StringBuilder out;

    private boolean inFeature  = false;
    private boolean inScenario = false;
    private boolean inExamples = false;

    private Table  table;
    private String tag;

    public String getResult() {
        return out.toString();
    }

    @Override
    public void start() {
        out = new StringBuilder(DEFAULT_BUFFER_SIZE);
        inFeature = false;
        inScenario = false;
        inExamples = false;
        table = null;
        tag = null;
    }

    @Override
    public void onFeature(String name, String description) {
        out.append(EOL);
        out.append("Feature: ");
        out.append(name);
        out.append(EOL);
        if (description != null) {
            out.append(IDENT);
            out.append(description);
            out.append(EOL);
        }
        inFeature = true;
        tag = null;
    }

    @Override
    public void onBackground(String name) {
        flushTable();
        out.append(EOL);
        out.append(IDENT);
        out.append("Background: ");
        if (name != null) {
            out.append(name);
        }
        inScenario = true;
        inExamples = false;
        tag = null;
    }

    @Override
    public void onScenario(String name) {
        flushTable();
        out.append(EOL);
        out.append(IDENT);
        out.append("Scenario: ");
        if (name != null) {
            out.append(name);
        }
        inScenario = true;
        inExamples = false;
        tag = null;
    }

    @Override
    public void onScenarioOutline(String name) {
        flushTable();
        out.append(EOL);
        out.append(IDENT);
        out.append("Scenario Outline: ");
        if (name != null) {
            out.append(name);
        }
        inScenario = true;
        inExamples = false;
        tag = null;
    }

    @Override
    public void onExamples(String name) {
        flushTable();
        out.append(EOL);
        out.append(IDENT);
        out.append("Examples: ");
        if (name != null) {
            out.append(name);
        }
        inExamples = true;
    }

    @Override
    public void onStep(String stepType, String name) {
        flushTable();
        out.append(EOL);
        out.append(IDENT);
        out.append(IDENT);
        out.append(stepType);
        out.append(' ');
        out.append(name);
    }

    @Override
    public void onTableRow(String[] cells) {
        if (table == null) {
            table = new Table(cells);
        } else {
            table.add(cells);
        }
    }

    @Override
    public void onTag(String name) {
        flushTable();
        if (tag == null) {
            tag = name;
            out.append(EOL);
            out.append(EOL);
            if (inFeature) {
                out.append(IDENT);
            }
        }
        out.append(name);
        out.append(' ');
    }

    @Override
    public void onPyString(String pyString) {
        out.append(EOL);
        if (pyString.startsWith(Lexer.PYSTRING)) {
            pyString = pyString.substring(3);
        }
        if (pyString.endsWith(Lexer.PYSTRING)) {
            pyString = pyString.substring(0, pyString.length() - 3).trim();
        }
        out.append(IDENT);
        out.append(Lexer.PYSTRING);
        out.append(EOL);
        out.append(pyString);
        out.append(EOL);
        out.append(IDENT);
        out.append(Lexer.PYSTRING);
    }

    @Override
    public void onComment(String comment, boolean hasNewLineBefore) {
        if (hasNewLineBefore) {
            out.append(EOL);
            if (inFeature) {
                out.append(IDENT);
            }
            if (inScenario) {
                out.append(IDENT);
            }
        }
        if (table == null) {
            out.append(comment);
        } else {
            table.add(comment, hasNewLineBefore);
        }
    }

    @Override
    public void onText(String text) {
        // todo bug here: some tags are passed as text
        tag = null;
        flushTable();
        out.append(EOL);
        out.append(text);
    }

    @Override
    public void onWhitespaces(String whitespaces) {
    }

    @Override
    public void end() {
        flushTable();
        out.append(EOL);
    }

    private void flushTable() {
        if (table != null) {
            for (Object rowOrComment : table.rows) {
                if (rowOrComment instanceof String) {
                    out.append(rowOrComment);
                } else if (rowOrComment instanceof String[]) {
                    String[] cells = (String[]) rowOrComment;
                    out.append(EOL);
                    out.append(IDENT);
                    out.append(IDENT);
                    if (!inExamples) {
                        out.append(IDENT);
                    }
                    for (int i = 0, columns = cells.length; i < columns; i++) {
                        out.append(column(cells[i], table.sizes[i]));
                    }
                    out.append("|");
                }
            }
            table = null;
        }
    }

    private static StringBuilder column(String cell, int size) {
        StringBuilder column = new StringBuilder(size + 3);
        column.append('|');
        column.append(' ');
        column.append(cell);
        int spaces = size - cell.length();
        for (int i = 0; i < spaces; i++) {
            column.append(' ');
        }
        column.append(' ');
        return column;
    }

    private static class Table {
        int columns = 0;
        int[] sizes;
        List rows = new LinkedList<>();

        Table(String[] header) {
            columns = header.length;
            sizes = new int[columns];
            add(header);
        }

        void add(String[] row) {
            if (row.length != columns) {
                throw new RuntimeException("row.length != columns");
            }
            rows.add(row);
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = Math.max(sizes[i], row[i] == null ? 0 : row[i].length());
            }
        }

        void add(String comment, boolean hasNewLineBefore) {
            if (hasNewLineBefore) {
                rows.add("\n");
            }
            rows.add(comment);
        }
    }
}
