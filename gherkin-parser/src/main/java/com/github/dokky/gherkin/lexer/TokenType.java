package com.github.dokky.gherkin.lexer;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum TokenType {
    COMMENT, WHITESPACE, TEXT,
    FEATURE_KEYWORD(true), BACKGROUND_KEYWORD(true), SCENARIO_KEYWORD(true), SCENARIO_OUTLINE_KEYWORD(true), EXAMPLES_KEYWORD(true),
    STEP_KEYWORD(true),
    TAG, PYSTRING, COLON, PIPE, TABLE_CELL;

    private boolean keyword = false;

    private TokenType() {
    }

    private TokenType(boolean keyword) {
        this.keyword = keyword;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public static Map<String, TokenType> KEYWORDS = new ImmutableMap.Builder<String, TokenType>()
                                                            .put("Feature", FEATURE_KEYWORD)
                                                            .put("Background", BACKGROUND_KEYWORD)
                                                            .put("Scenario", SCENARIO_KEYWORD)
                                                            .put("Scenario Outline", SCENARIO_OUTLINE_KEYWORD)
                                                            .put("Examples", EXAMPLES_KEYWORD)
                                                            .put("Scenarios", EXAMPLES_KEYWORD)
                                                            .put("Given", STEP_KEYWORD)
                                                            .put("When", STEP_KEYWORD)
                                                            .put("Then", STEP_KEYWORD)
                                                            .put("But", STEP_KEYWORD)
                                                            .put("And", STEP_KEYWORD).build();

}
