package com.github.dokky.gherkin.model;

import lombok.Data;

@Data
public class TableRow extends GherkinElement {
    private final String[] cells;
}
