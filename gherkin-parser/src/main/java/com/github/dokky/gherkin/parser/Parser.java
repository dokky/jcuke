package com.github.dokky.gherkin.parser;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import static com.github.dokky.gherkin.parser.LexerTokenType.*;

@Slf4j
public class Parser {

    public void parse(String text, FeatureHandler handler) {
        Lexer lexer = new Lexer();
        lexer.start(text);
        LexerTokenType currentToken = null;
        while ((currentToken = lexer.getMyCurrentToken()) != null) {
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
                int lastPosition = lexer.getMyPosition();
                int lastState = lexer.getMyState();
                while (lexer.getMyCurrentToken() == TABLE_CELL || lexer.getMyCurrentToken() == PIPE) {

                    if (lexer.getMyCurrentToken() == TABLE_CELL) {
                        rows.add(lexer.getCurrentTokenValue());
                    }

                    lastPosition = lexer.getMyPosition();
                    lastState = lexer.getMyState();
                    lexer.advance();

                    if ((lexer.getMyCurrentToken() == WHITESPACE || lexer.getMyCurrentToken() == COMMENT) && lexer.hasNewLine(lastPosition, lexer.getMyPosition())) {
                        handler.onTableRow(rows.toArray(new String[rows.size()]));
                        rows.clear();
                    }
                    if (lexer.getMyCurrentToken() == WHITESPACE) {
                        lexer.advance();
                    }
                }
                lexer.setMyPosition(lastPosition); // return position back to last text occurrence
                lexer.setMyState(lastState);
            } else if (currentToken == FEATURE_KEYWORD) {
                handler.onFeature(getTextLine(lexer), getTextBlock(lexer));
            } else if (currentToken == TEXT || currentToken == COLON) {
                handler.onText(lexer.getCurrentTokenValue());
            }

            lexer.advance();
        }
    }

    private String getTextLine(Lexer lexer) {
        int tokenPosition = lexer.getMyPosition();
        StringBuilder text = new StringBuilder();
        while (lexer.getMyCurrentToken() != COMMENT && !lexer.hasNewLine(tokenPosition, lexer.getMyPosition())) {
            lexer.advance();
            if (lexer.getMyCurrentToken() == TEXT) {
                text.append(lexer.getCurrentTokenValue());
            }
        }
        return text.length() == 0 ? null : text.toString();
    }

    private String getTextBlock(Lexer lexer) {
        int lastPosition = lexer.getMyPosition();
        int lastState = lexer.getMyState();
        StringBuilder text = new StringBuilder();
        while (lexer.getMyCurrentToken() != null && !lexer.getMyCurrentToken().isKeyword()) {
            lexer.advance();
            if (lexer.getMyCurrentToken() == TEXT) {
                text.append(lexer.getCurrentTokenValue());
                lastPosition = lexer.getMyPosition();
                lastState = lexer.getMyState();
            } else if (lexer.getMyCurrentToken() == WHITESPACE) {
                text.append('\n');
            }
        }
        lexer.setMyPosition(lastPosition); // return position back to last text occurrence
        lexer.setMyState(lastState);
        return text.length() == 0 ? null : text.toString().trim();
    }
}
