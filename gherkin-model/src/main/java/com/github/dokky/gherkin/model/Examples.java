package com.github.dokky.gherkin.model;


import lombok.Data;

@Data
public class Examples extends GherkinElement {
    private String name;
    private Table  table;
}
