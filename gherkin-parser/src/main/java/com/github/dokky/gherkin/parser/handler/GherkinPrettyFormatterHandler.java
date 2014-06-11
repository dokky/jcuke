package com.github.dokky.gherkin.parser.handler;

import com.github.dokky.gherkin.lexer.GherkinLexer;
import com.github.dokky.gherkin.model.GherkinParseException;
import com.github.dokky.gherkin.model.Table;
import com.github.dokky.gherkin.model.TableRow;
import com.github.dokky.gherkin.parser.GherkinParserHandler;
import com.sun.istack.internal.Nullable;
import org.apache.commons.lang.StringUtils;

public class GherkinPrettyFormatterHandler implements GherkinParserHandler {
    private final static int DEFAULT_BUFFER_SIZE = 350 * 1024;
    private final static String IDENT = "    ";
    private final static String DOUBLE_IDENT = IDENT + IDENT;
    private final char EOL = '\n';

    private StringBuilder out;

    private boolean inFeature = false;
    private boolean inScenario = false;
    private boolean inExamples = false;

    private TableWithColumnSizes table;
    private String tag;

    public String getResult() {
        return StringUtils.stripStart(out.toString(), null);
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
            String[] lines = description.split("\n");
            for (String line : lines) {
                out.append(IDENT);
                out.append(line);
                out.append(EOL);
            }
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
        out.append(DOUBLE_IDENT);
        out.append(stepType);
        out.append(' ');
        out.append(name);
    }

    @Override
    public void onTableRow(String[] cells) {
        if (table == null) {
            table = new TableWithColumnSizes(cells); // header
        } else {
            table.addRow(cells); // rows with cells
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
        if (pyString.startsWith(GherkinLexer.PYSTRING)) {
            pyString = pyString.substring(3);
        }
        if (pyString.endsWith(GherkinLexer.PYSTRING)) {
            pyString = pyString.substring(0, pyString.length() - 3);
        }
        out.append(DOUBLE_IDENT);
        out.append(GherkinLexer.PYSTRING);
        out.append(EOL);
        String[] lines = pyString.replaceAll("\t", IDENT).split("\n");
        int minNumberOfSpaces = Integer.MAX_VALUE;
        for (String line : lines) {
            if (line.trim().length() == 0) {
                continue;
            }
            int spaceCount = 0;
            while (spaceCount < line.length()) {
                if (line.charAt(spaceCount) == ' ') {
                    spaceCount++;
                } else {
                    break;
                }
            }
            minNumberOfSpaces = Math.min(minNumberOfSpaces, spaceCount);
        }
        for (String line : lines) {
            if (line.trim().length() == 0) {
                continue;
            }
            out.append(DOUBLE_IDENT);
            out.append(line.substring(minNumberOfSpaces));
            out.append(EOL);
        }
        out.append(DOUBLE_IDENT);
        out.append(GherkinLexer.PYSTRING);
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
            table.addComment(comment, hasNewLineBefore);
        }
    }

    @Override
    public void onText(String text) {
        if (!text.startsWith("Using step definitions from:")) {  // hack/support for freshen framework
            // todo bug here: some tags are passed as text
            if (!text.isEmpty()) {
                throw new GherkinParseException("Unexpected text token: " + text);
            }
            tag = null;
            flushTable();
        }
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
            printRow(table.getHeadings());
            for (TableRow rowWithComments : table.getRows()) {
                printRow(rowWithComments);
            }
            table = null;
        }
    }

    private void printRow(TableRow rowOrComment) {
        String[] cells = rowOrComment.getCells();
        out.append(EOL);
        out.append(DOUBLE_IDENT);
        if (!inExamples) {
            out.append(IDENT);
        }
        for (int i = 0, columns = cells.length; i < columns; i++) {
            out.append(column(cells[i], table.sizes[i]));
        }
        out.append("|");
        if (!rowOrComment.getComments().isEmpty()) {
            for (String comment : rowOrComment.getComments()) {
                out.append(' ').append(comment.trim());
            }
        }
    }

    private static StringBuilder column(@Nullable String cellValue, int size) {
        String cell = cellValue == null ? "" : cellValue;
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

    private static class TableWithColumnSizes extends Table {
        int[] sizes;

        public TableWithColumnSizes(String[] header) {
            super(header);
            sizes = new int[header.length];
        }

        public void addRow(String[] row) {
            super.addRow(row);

            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = Math.max(sizes[i], row[i] == null ? 0 : row[i].length());
                while (sizes[i] % 4 != 0) {
                    sizes[i]++;
                }
            }
        }

        void addComment(String comment, boolean hasNewLineBefore) {
            if (hasNewLineBefore) {
                getLastRow().getComments().add("\n");
            }
            getLastRow().getComments().add(comment);
        }
    }
}
