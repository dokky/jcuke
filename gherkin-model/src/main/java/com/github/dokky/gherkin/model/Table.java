package com.github.dokky.gherkin.model;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;

@Data
public class Table {

    private TableRow headings;
    private final LinkedList<TableRow> rows = new LinkedList<>();

    public Table() {
    }

    public Table(String[] headings) {
        this(new TableRow(headings));
    }

    public Table(TableRow headings) {
        setHeadings(headings);
    }

    public void setHeadings(TableRow headings) {
        for (int i = 0; i < headings.getCells().length; i++) {
            if (StringUtils.isBlank(headings.getCells()[i])) {
                throw new GherkinParseException("Empty header at position: " + (i + 1));
            }
        }

        this.headings = headings;
    }

    public void addRow(String[] row) {
        addRow(new TableRow(row));
    }

    public void addRow(TableRow row) {
        if (headings == null) {
            setHeadings(row);
            return;
        }

        if (row.getCells().length != headings.getCells().length) {
            throw new GherkinParseException("row.length != header.length. Details: " +
                    "header: " + Arrays.toString(headings.getCells())
                    + " row: " + Arrays.toString(row.getCells()));
        }
        rows.add(row);
    }

    public TableRow getLastRow() {
        return rows.isEmpty() ? headings : rows.getLast();
    }
}
