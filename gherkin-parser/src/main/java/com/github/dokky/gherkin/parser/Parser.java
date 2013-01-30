package com.github.dokky.gherkin.parser;

import com.github.dokky.gherkin.lexer.Lexer;
import com.github.dokky.gherkin.lexer.TokenType;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import static com.github.dokky.gherkin.lexer.TokenType.*;

@Slf4j
public class Parser {
    private FeatureHandler handler;

    public Parser(FeatureHandler handler) {
        this.handler = handler;
    }

    public void parse(String text) {
        Lexer lexer = new Lexer(text);
        handler.start();
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
                String name = getNextTextToken(lexer);
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
                handler.onStep(lexer.getCurrentTokenValue(), getNextTextToken(lexer));
            } else if (currentToken == TABLE_CELL) {
                List<String> row = new LinkedList<String>();
                while (currentToken != null &&
                       (currentToken == TABLE_CELL ||
                        currentToken == PIPE ||
                        currentToken == WHITESPACE ||
                        currentToken == COMMENT)) {

                    String value = lexer.getCurrentTokenValue();
                    if (currentToken == TABLE_CELL) {
                        row.add(value);
                    } else if (currentToken == COMMENT) {
                        if (!row.isEmpty()) {
                            handler.onTableRow(row.toArray(new String[row.size()]));
                            row.clear();
                        }
                        handler.onComment(value);
                    } else if (currentToken == WHITESPACE) {
                        if (value.contains("\n") && !row.isEmpty()) {
                            handler.onTableRow(row.toArray(new String[row.size()]));
                            row.clear();
                        }
                        handler.onWhitespaces(value);
                    }
                    lexer.parseNextToken();
                    currentToken = lexer.getCurrentTokenType();
                }
                continue; // go to main loop
            } else if (currentToken == FEATURE_KEYWORD) {
                handler.onFeature(getNextTextToken(lexer), collectTextItemsUntilScenarioStarts(lexer));
            } else if (currentToken == TEXT || currentToken == COLON) {
                handler.onText(lexer.getCurrentTokenValue());
            }

            lexer.parseNextToken();
        }
        handler.end();
    }

    private String getNextTextToken(Lexer lexer) {
        int previousPosition = lexer.getCurrentPosition();
        lexer.parseNextToken();
        while (lexer.getCurrentTokenType() == COLON || lexer.getCurrentTokenType() == TEXT || lexer.getCurrentTokenType() == WHITESPACE || lexer.getCurrentTokenType() == COMMENT) {
            if (lexer.getCurrentTokenType() == TEXT) {
                return lexer.getCurrentTokenValue();
            } else if (lexer.getCurrentTokenType() == COMMENT || lexer.hasNewLine(previousPosition, lexer.getCurrentPosition())) {
                lexer.setCurrentPosition(previousPosition); // return one token back
                break;
            }
            previousPosition = lexer.getCurrentPosition();
            lexer.parseNextToken();
        }
        return null;
    }

    private String collectTextItemsUntilScenarioStarts(Lexer lexer) {
        int lastPosition = lexer.getCurrentPosition();
        StringBuilder text = new StringBuilder();
        while (lexer.getCurrentTokenType() != null && !lexer.getCurrentTokenType().isScenarioKeyword()) {
            lexer.parseNextToken();
            if (lexer.getCurrentTokenType() == TEXT) {
                text.append(lexer.getCurrentTokenValue());
                lastPosition = lexer.getCurrentPosition();
            } else if (lexer.getCurrentTokenType() == WHITESPACE) {
                text.append('\n');
            }
        }
        lexer.setCurrentPosition(lastPosition); // return position back to last text occurrence
        return text.length() == 0 ? null : text.toString().trim();
    }
}
