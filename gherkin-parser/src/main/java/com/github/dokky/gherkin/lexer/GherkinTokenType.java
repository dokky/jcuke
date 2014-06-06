package com.github.dokky.gherkin.lexer;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum GherkinTokenType {
    COMMENT, WHITESPACE, TEXT,
    FEATURE_KEYWORD(true), BACKGROUND_KEYWORD(true), SCENARIO_KEYWORD(true), SCENARIO_OUTLINE_KEYWORD(true), EXAMPLES_KEYWORD(true),
    STEP_KEYWORD(true),
    TAG, PYSTRING, COLON, PIPE, TABLE_CELL;

    private boolean keyword = false;

    private GherkinTokenType() {
    }

    private GherkinTokenType(boolean keyword) {
        this.keyword = keyword;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public boolean isScenarioKeyword() {
        return this == BACKGROUND_KEYWORD || this == SCENARIO_KEYWORD || this == SCENARIO_OUTLINE_KEYWORD;
    }


    public static Map<String, GherkinTokenType> SCENARIO_KEYWORDS = new ImmutableMap.Builder<String, GherkinTokenType>()
            .put("Background", BACKGROUND_KEYWORD)
            .put("Scenario", SCENARIO_KEYWORD)
            .put("Scenario Outline", SCENARIO_OUTLINE_KEYWORD).build();

    public static Map<String, GherkinTokenType> STEP_KEYWORDS = new ImmutableMap.Builder<String, GherkinTokenType>()
            .put("Given", STEP_KEYWORD)
            .put("When", STEP_KEYWORD)
            .put("Then", STEP_KEYWORD)
            .put("But", STEP_KEYWORD)
            .put("And", STEP_KEYWORD).build();

    public static Map<String, GherkinTokenType> KEYWORDS = new ImmutableMap.Builder<String, GherkinTokenType>()
            .put("Feature", FEATURE_KEYWORD)
            .put("Examples", EXAMPLES_KEYWORD)
            .put("Scenarios", EXAMPLES_KEYWORD)
            .putAll(SCENARIO_KEYWORDS)
            .putAll(STEP_KEYWORDS).build();

}
