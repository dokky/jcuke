package com.github.dokky.gherkin.model;


import lombok.Data;
import lombok.NonNull;

@Data
public class Step extends GherkinElement {
    private final @NonNull String   stepType;
    private final @NonNull String   step;
    private                Table    table;
    private                PyString pyString;
}
