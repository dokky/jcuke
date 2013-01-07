package com.github.dokky.gherkin.model;


import lombok.Data;
import lombok.NonNull;

@Data
public class Tag extends GherkinElement {
    private final @NonNull String name;
}
