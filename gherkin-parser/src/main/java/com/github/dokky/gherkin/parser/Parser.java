package com.github.dokky.gherkin.parser;

import java.util.LinkedList;
import java.util.List;

import com.github.dokky.gherkin.lexer.Lexer;
import com.github.dokky.gherkin.lexer.TokenType;

import static com.github.dokky.gherkin.lexer.TokenType.*;

public class Parser {
    private FeatureHandler handler;

    public Parser(FeatureHandler handler) {
        this.handler = handler;
    }

    @SuppressWarnings("ConstantConditions")
    public void parse(String text) {
        Lexer lexer = new Lexer(text);
        try {
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
                    TokenType previousTokenType = lexer.getPreviousTokenType();
                    boolean hasNewLineBefore = true;
                    if (previousTokenType != null) {
                        hasNewLineBefore = previousTokenType == WHITESPACE && lexer.hasNewLine(lexer.getPreviousTokenStartPosition(), lexer.getCurrentTokenStartPosition());
                    }
                    handler.onComment(lexer.getCurrentTokenValue(), hasNewLineBefore);
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
                    List<String> row = new LinkedList<>();
                    while (currentToken != null &&
                           (currentToken == TABLE_CELL ||
                            currentToken == PIPE ||
                            currentToken == WHITESPACE ||
                            currentToken == COMMENT)) {

                        String value = lexer.getCurrentTokenValue();
                        if (currentToken == TABLE_CELL) {
                            row.add(value);
                        } else if (currentToken == PIPE) {
                            TokenType previousTokenType = lexer.getPreviousTokenType();
                            if (previousTokenType == PIPE) { // case when table has empty cells like |abc||bdx|
                                row.add(null);
                            } else if (previousTokenType == WHITESPACE) { // case when table has empty cells like |abc|  |bdx|
                                if (lexer.hasNewLine(lexer.getPreviousTokenStartPosition(), lexer.getCurrentTokenStartPosition())) {
                                    lexer.parseNextToken();
                                    currentToken = lexer.getCurrentTokenType();
                                    continue;
                                }
                                else if(lexer.charAt(lexer.getPreviousTokenStartPosition() - 1) == '|') {  // hack
                                    row.add(null);
                                }
                            }
                        } else if (currentToken == COMMENT) {
                            if (!row.isEmpty()) {
                                handler.onTableRow(row.toArray(new String[row.size()]));
                                row.clear();
                            }
                            TokenType previousTokenType = lexer.getPreviousTokenType();
                            boolean hasNewLineBefore = true;
                            if (previousTokenType != null) {
                                hasNewLineBefore = previousTokenType == WHITESPACE && lexer.hasNewLine(lexer.getPreviousTokenStartPosition(), lexer.getCurrentTokenStartPosition());
                            }
                            handler.onComment(lexer.getCurrentTokenValue(), hasNewLineBefore);
                        } else if (currentToken == WHITESPACE) {
                            if (value.contains("\n") && !row.isEmpty()) {
                                handler.onTableRow(row.toArray(new String[row.size()]));
                                row.clear();
                            }
                            handler.onWhitespaces(value);
                        }
                        lexer.parseNextToken();
                        currentToken = lexer.getCurrentTokenType();
                        if (currentToken == null && !row.isEmpty()) {
                            handler.onTableRow(row.toArray(new String[row.size()]));
                            row.clear();
                        }
                    } // while end

                    continue; // go to main loop
                } else if (currentToken == FEATURE_KEYWORD) {
                    handler.onFeature(getNextTextToken(lexer), collectTextItemsUntilScenarioStarts(lexer));
                } else if (currentToken == TEXT || currentToken == COLON) {
                    handler.onText(lexer.getCurrentTokenValue());
                }

                lexer.parseNextToken();
            }

            handler.end();
        } catch (Exception e) {
            throw new RuntimeException("Error during parsing at line " + lexer.getCurrentLineNumber() + ": " + e.getMessage(), e);
        }
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
        String trimmed = text.toString().trim();
        return trimmed.length() == 0 ? null : trimmed;
    }
}
