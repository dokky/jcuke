package com.github.dokky.gherkin.model;


import lombok.Data;

@Data
public class Background extends Scenario {
    public Background(String name) {
        super(name);
    }
}
