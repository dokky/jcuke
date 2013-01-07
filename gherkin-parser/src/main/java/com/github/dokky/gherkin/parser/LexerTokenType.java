package com.github.dokky.gherkin.parser;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public enum LexerTokenType {
    COMMENT, WHITESPACE, TEXT,
    FEATURE_KEYWORD(true), BACKGROUND_KEYWORD(true), SCENARIO_KEYWORD(true), SCENARIO_OUTLINE_KEYWORD(true), EXAMPLES_KEYWORD(true),
    STEP_KEYWORD(true),
    TAG, PYSTRING, COLON, PIPE, TABLE_CELL;

    private boolean keyword = false;

    private LexerTokenType() {
    }

    private LexerTokenType(boolean keyword) {
        this.keyword = keyword;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public static Map<String, LexerTokenType> KEYWORDS = new ImmutableMap.Builder<String, LexerTokenType>()
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
