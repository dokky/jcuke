package com.github.dokky.gherkin.parser;

import com.github.dokky.gherkin.lexer.Lexer;
import com.github.dokky.gherkin.lexer.TokenType;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import static com.github.dokky.gherkin.lexer.TokenType.*;

@Slf4j
public class Parser {

    public void parse(String text, FeatureHandler handler) {
        Lexer lexer = new Lexer(text);
        lexer.parseNextToken();
        TokenType currentToken = lexer.getCurrentTokenType();
        while (currentToken != null) {
            currentToken = lexer.getCurrentTokenType();
            if (currentToken == WHITESPACE) {
                handler.onWhitespaces(lexer.getCurrentTokenValue());
            } else if (currentToken == TAG) {
                handler.onTag(lexer.getCurrentTokenValue());
            } else if (currentToken == COMMENT) {
                handler.onComment(lexer.getCurrentTokenValue());
            } else if (currentToken == PYSTRING) {
                handler.onPyString(lexer.getCurrentTokenValue());
            } else if (currentToken == SCENARIO_KEYWORD || currentToken == SCENARIO_OUTLINE_KEYWORD || currentToken == BACKGROUND_KEYWORD || currentToken == EXAMPLES_KEYWORD) {
                String name = getTextLine(lexer);
                if (currentToken == SCENARIO_KEYWORD) {
                    handler.onScenario(name);
                } else if (currentToken == SCENARIO_OUTLINE_KEYWORD) {
                    handler.onScenarioOutline(name);
                } else if (currentToken == BACKGROUND_KEYWORD) {
                    handler.onBackground(name);
                } else if (currentToken == EXAMPLES_KEYWORD) {
                    handler.onExamples(name);
                }
            } else if (currentToken == STEP_KEYWORD) {
                handler.onStep(lexer.getCurrentTokenValue(), getTextLine(lexer));
            } else if (currentToken == TABLE_CELL) {
                List<String> rows = new LinkedList<String>();
                int lastPosition = lexer.getCurrentPosition();
                int lastState = lexer.getState();
                while (lexer.getCurrentTokenType() == TABLE_CELL || lexer.getCurrentTokenType() == PIPE) {

                    if (lexer.getCurrentTokenType() == TABLE_CELL) {
                        rows.add(lexer.getCurrentTokenValue());
                    }

                    lastPosition = lexer.getCurrentPosition();
                    lastState = lexer.getState();
                    lexer.parseNextToken();

                    if ((lexer.getCurrentTokenType() == WHITESPACE || lexer.getCurrentTokenType() == COMMENT) && lexer.hasNewLine(lastPosition, lexer.getCurrentPosition())) {
                        handler.onTableRow(rows.toArray(new String[rows.size()]));
                        rows.clear();
                    }
                    if (lexer.getCurrentTokenType() == WHITESPACE) {
                        lexer.parseNextToken();
                    }
                }
                lexer.setCurrentPosition(lastPosition); // return position back to last text occurrence
                lexer.setState(lastState);
            } else if (currentToken == FEATURE_KEYWORD) {
                handler.onFeature(getTextLine(lexer), getTextBlock(lexer));
            } else if (currentToken == TEXT || currentToken == COLON) {
                handler.onText(lexer.getCurrentTokenValue());
            }

            lexer.parseNextToken();
        }
    }

    private String getTextLine(Lexer lexer) {
        int tokenPosition = lexer.getCurrentPosition();
        StringBuilder text = new StringBuilder();
        while (lexer.getCurrentTokenType() != COMMENT && !lexer.hasNewLine(tokenPosition, lexer.getCurrentPosition())) {
            lexer.parseNextToken();
            if (lexer.getCurrentTokenType() == TEXT) {
                text.append(lexer.getCurrentTokenValue());
            }
        }
        return text.length() == 0 ? null : text.toString();
    }

    private String getTextBlock(Lexer lexer) {
        int lastPosition = lexer.getCurrentPosition();
        int lastState = lexer.getState();
        StringBuilder text = new StringBuilder();
        while (lexer.getCurrentTokenType() != null && !lexer.getCurrentTokenType().isKeyword()) {
            lexer.parseNextToken();
            if (lexer.getCurrentTokenType() == TEXT) {
                text.append(lexer.getCurrentTokenValue());
                lastPosition = lexer.getCurrentPosition();
                lastState = lexer.getState();
            } else if (lexer.getCurrentTokenType() == WHITESPACE) {
                text.append('\n');
            }
        }
        lexer.setCurrentPosition(lastPosition); // return position back to last text occurrence
        lexer.setState(lastState);
        return text.length() == 0 ? null : text.toString().trim();
    }
}
