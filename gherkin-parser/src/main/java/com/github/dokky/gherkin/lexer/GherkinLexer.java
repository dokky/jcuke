package com.github.dokky.gherkin.lexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


public final class GherkinLexer {

    protected CharSequence buffer;
    protected int          startOffset;
    protected int          endOffset;
    @Getter
    @Setter
    private   int          currentPosition;
    @Getter
    private   int          currentTokenStartPosition;
    @Getter
    private   int          previousTokenStartPosition;
    @Getter
    private int currentLineNumber = 1;

    @Getter
    private GherkinTokenType currentTokenType;
    @Getter
    private GherkinTokenType previousTokenType;

    private static final int CONTEXT_ROOT             = 0;
    private static final int CONTEXT_FEATURE          = 1;
    private static final int CONTEXT_BACKGROUND       = 2;
    private static final int CONTEXT_SCENARIO         = 3;
    private static final int CONTEXT_SCENARIO_OUTLINE = 4;
    private static final int CONTEXT_EXAMPLES         = 5;
    private              int context                  = CONTEXT_ROOT;

    // todo replace with state
    private boolean firstStepInScenarioFound = false;
    private boolean afterFeatureKeyword      = false;
    private boolean afterStepKeyword         = false;
    private boolean afterScenarioKeyword     = false;
    private boolean afterExamplesKeyword     = false;
    private boolean inTable                  = false;

    public static final String PYSTRING = "\"\"\"";

    private static final List<String> SCENARIO_KEYWORDS = sort(GherkinTokenType.SCENARIO_KEYWORDS.keySet());
    private static final List<String> STEP_KEYWORDS     = sort(GherkinTokenType.STEP_KEYWORDS.keySet());


    public GherkinLexer(CharSequence buffer) {
        init(buffer, 0, buffer.length());
    }

    public void init(CharSequence buffer, int startOffset, int endOffset) {
        this.buffer = buffer;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        previousTokenStartPosition = startOffset;
        currentPosition = startOffset;
        context = CONTEXT_ROOT;
        currentLineNumber = 1;
        firstStepInScenarioFound = false;
        afterStepKeyword = false;
        afterScenarioKeyword = false;
        afterExamplesKeyword = false;
        inTable = false;
        afterFeatureKeyword = false;
    }

    public String getCurrentTokenValue() {
        return buffer.subSequence(currentTokenStartPosition, currentPosition).toString();
    }

    public String getPreviousTokenValue() {
        if (previousTokenStartPosition > 0 && previousTokenStartPosition != currentTokenStartPosition) {
            return buffer.subSequence(previousTokenStartPosition, currentTokenStartPosition).toString();
        } else {
            return null;
        }
    }

    public char charAt(int index) {
        return buffer.charAt(index);
    }

    public void parseNextToken() {
        if (currentPosition >= endOffset) {
            currentTokenType = null;
            return;
        }
        previousTokenType = currentTokenType;
        previousTokenStartPosition = currentTokenStartPosition;
        currentTokenStartPosition = currentPosition;
        char c = buffer.charAt(currentPosition);

        if (Character.isWhitespace(c)) {
            currentTokenType = GherkinTokenType.WHITESPACE;
            while (currentPosition < endOffset && Character.isWhitespace(buffer.charAt(currentPosition))) {
                if (buffer.charAt(currentPosition) == '\n') {
                    currentLineNumber++;
                    // reset all flags after new line
                    afterFeatureKeyword = false;
                    afterScenarioKeyword = false;
                    afterStepKeyword = false;
                    afterExamplesKeyword = false;
                    inTable = false;
                }
                currentPosition++;
            }
            return;
        }
        if (context == CONTEXT_ROOT) {
            if (c == '@') {
                parseTag();
                return;
            } else if (c == '#') {
                parseComment();
                return;
            } else if (isStringAtPosition("Feature")) {
                int colonPosition = getColonPosition("Feature");
                if (colonPosition != 0) {
                    currentPosition = colonPosition;
                    currentTokenType = GherkinTokenType.FEATURE_KEYWORD;

                    context = CONTEXT_FEATURE;
                    afterFeatureKeyword = true;
                    return;
                }
            }
        }

        if (context == CONTEXT_SCENARIO || context == CONTEXT_SCENARIO_OUTLINE || context == CONTEXT_BACKGROUND) {
            if (c == '#') {
                parseComment();
                return;
            } else if (firstStepInScenarioFound && isStringAtPosition(PYSTRING) && isValidPyString()) {
                parsePyString();
                return;
            } else if (firstStepInScenarioFound && c == '|') {
                parsePipe();
                return;
            } else if (firstStepInScenarioFound && inTable) {
                parseTableCell();
                return;
            } else if (!afterScenarioKeyword && !afterStepKeyword) {

                for (String keyword : STEP_KEYWORDS) {

                    if (isStringAtPosition(keyword)) {

                        int length = keyword.length();

                        if (endOffset - currentPosition > length && !Character.isLetterOrDigit(buffer.charAt(currentPosition + length))) {
                            currentPosition += length;
                            currentTokenType = GherkinTokenType.KEYWORDS.get(keyword);
                            firstStepInScenarioFound = true;
                            afterStepKeyword = true;
                            return;
                        }
                    }
                }

                if (context == CONTEXT_SCENARIO_OUTLINE) {
                    if (isStringAtPosition("Examples")) { // todo scenarios
                        int colonPosition = getColonPosition("Examples");
                        if (colonPosition != 0) {
                            currentPosition = colonPosition;
                            currentTokenType = GherkinTokenType.EXAMPLES_KEYWORD;
                            context = CONTEXT_EXAMPLES;
                            afterExamplesKeyword = true;
                            return;
                        }
                    }
                }
                context = CONTEXT_FEATURE;
            }
        }

        if (context == CONTEXT_EXAMPLES) {
            if (c == '#') {
                parseComment();
                return;
            } else if (c == '|') {
                parsePipe();
                return;
            } else if (inTable) {
                parseTableCell();
                return;
            } else if (!afterExamplesKeyword) {
                context = CONTEXT_FEATURE;
            }
        }


        if (context == CONTEXT_FEATURE) {
            if (c == '@') {
                parseTag();
                return;
            } else if (c == '#') {
                parseComment();
                return;
            } else {
                for (String scenarioKeyword : SCENARIO_KEYWORDS) {
                    if (isStringAtPosition(scenarioKeyword)) {
                        int colonPosition = getColonPosition(scenarioKeyword);
                        if (colonPosition != 0) {
                            currentPosition = colonPosition;
                            currentTokenType = GherkinTokenType.SCENARIO_KEYWORDS.get(scenarioKeyword);

                            firstStepInScenarioFound = false;
                            switch (scenarioKeyword) {
                                case "Scenario Outline":
                                    context = CONTEXT_SCENARIO_OUTLINE;
                                    break;
                                case "Scenario":
                                    context = CONTEXT_SCENARIO;
                                    break;
                                default:
                                    context = CONTEXT_BACKGROUND;
                                    break;
                            }
                            afterScenarioKeyword = true;
                            return;
                        }
                    }
                }
            }
        }


        currentTokenType = GherkinTokenType.TEXT;
        if (afterFeatureKeyword) {
            parseToEol();
        } else {
            parseToEolOrComment();
        }
    }

    private void parsePipe() {
        currentTokenType = GherkinTokenType.PIPE;
        currentPosition++;
        inTable = true;
    }

    private void parseTableCell() {
        currentTokenType = GherkinTokenType.TABLE_CELL;

        while (currentPosition < endOffset && buffer.charAt(currentPosition) != '\n' && (buffer.charAt(currentPosition) != '|' || (buffer.charAt(currentPosition - 1) == '\\'))) {
            currentPosition++;

        }
        while (currentPosition > 0 && Character.isWhitespace(buffer.charAt(currentPosition - 1))) {
            currentPosition--;
        }
    }

    private void parsePyString() {
        currentTokenType = GherkinTokenType.PYSTRING;
        currentPosition += 3; // first occurrence of '"""'
        while (currentPosition < endOffset) {
            if (buffer.charAt(currentPosition) == '\n') {
                currentLineNumber++;
            }
            if (isStringAtPosition(PYSTRING)) { // last occurrence of '"""'
                currentPosition += 3;
                if (isValidPyString()) { // verify pyString is on the line itself
                    return;
                }
            } else {
                currentPosition++;
            }

        }

    }

    private boolean isValidPyString() {
        return getCurrentLine(buffer, currentPosition, endOffset).trim().equals(PYSTRING);
    }

    public static String getCurrentLine(CharSequence buffer, int currentPosition, int endOffset) {
        int lineStart = 0, lineEnd = 0;
        int position = currentPosition;
        while (position > 0) {
            position--;
            if (buffer.charAt(position) == '\n') {
                position++;
                break;
            }
        }
        lineStart = position;
        while (position++ < endOffset - 1) {

            if (buffer.charAt(position) == '\n') {
                position++;
                break;
            }
        }
        lineEnd = position;

        return buffer.subSequence(lineStart, lineEnd).toString();
    }

    private void parseComment() {
        currentTokenType = GherkinTokenType.COMMENT;
        currentPosition++;
        while (currentPosition < endOffset && buffer.charAt(currentPosition) != '\n') {
            currentPosition++;
        }
    }

    private void parseTag() {
        currentTokenType = GherkinTokenType.TAG;
        currentPosition++;
        while (currentPosition < endOffset && isValidTagChar(buffer.charAt(currentPosition))) {
            currentPosition++;
        }
    }

    private static boolean isValidTagChar(char c) {
        return !Character.isWhitespace(c) && c != '@';
    }

    private boolean isStringAtPosition(String keyword) {
        int length = keyword.length();
        return endOffset - currentPosition >= length && buffer.subSequence(currentPosition, currentPosition + length).toString().equals(keyword);
    }


    private void parseToEol() {
        currentPosition++;
        while (currentPosition < endOffset && buffer.charAt(currentPosition) != '\n') {
            currentPosition++;
        }
    }

    private void parseToEolOrComment() {
        currentPosition++;
        while (currentPosition < endOffset && buffer.charAt(currentPosition) != '\n' && buffer.charAt(currentPosition) != '#') {
            currentPosition++;
        }
    }


    private static List<String> sort(Collection<String> keywords) {
        List<String> keywordList = new ArrayList<String>(keywords);
        Collections.sort(keywordList, new Comparator<String>() {
            public int compare(String x0, String x1) {
                return x1.length() - x0.length();
            }
        });
        return keywordList;
    }

    public boolean hasNewLine(int start, int end) {
        while (start < end && start < endOffset) {
            if (buffer.charAt(start++) == '\n') {
                return true;
            }
        }
        return false;
    }

    private int getColonPosition(String keyword) {
        int length = keyword.length();
        int colonPosition = currentPosition + length;
        while (colonPosition < endOffset) {
            if (Character.isWhitespace(buffer.charAt(colonPosition))) {
                colonPosition++;
            } else if (buffer.charAt(colonPosition) == ':') {
                return colonPosition + 1;
            } else {
                return 0;
            }
        }
        return 0;
    }


}
