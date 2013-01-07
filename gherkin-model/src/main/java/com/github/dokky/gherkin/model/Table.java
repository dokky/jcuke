package com.github.dokky.gherkin.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class Table extends GherkinElement {
    private TableRow headings;
    private final List<TableRow> rows = new LinkedList<TableRow>();
}
