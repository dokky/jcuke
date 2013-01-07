package com.github.dokky.gherkin.model;

import lombok.Data;

@Data
public class FeatureFile extends GherkinElement {
    private Feature feature;
}
