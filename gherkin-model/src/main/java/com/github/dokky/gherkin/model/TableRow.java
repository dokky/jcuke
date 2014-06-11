package com.github.dokky.gherkin.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class TableRow {
    private final String[] cells;
    private List<String> comments = new LinkedList<>();
}
