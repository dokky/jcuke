package com.github.dokky.gherkin.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.github.dokky.gherkin.parser.LexerTokenType.STEP_KEYWORD;

@Data
public class Lexer {
    protected CharSequence myBuffer;
    protected int          myStartOffset;
    protected int          myEndOffset;
    private   int          myPosition;
    private   int          myCurrentTokenStart;

    private int            myState;
    private LexerTokenType myCurrentToken;

    private static final int STATE_DEFAULT            = 0;
    private static final int STATE_AFTER_KEYWORD      = 1;
    private static final int STATE_AFTER_STEP_KEYWORD = 3;
    private static final int STATE_TABLE              = 2;

    private static final String PYSTRING_MARKER = "\"\"\"";

    private static final List<String> myKeywords = getKeywords();


    public void start(CharSequence buffer) {
        start(buffer, 0, buffer.length(), STATE_DEFAULT);
    }

    public void start(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        myBuffer = buffer;
        myStartOffset = startOffset;
        myEndOffset = endOffset;
        myPosition = startOffset;
        myState = initialState;
        advance();
    }

    public String getCurrentTokenValue() {
        return myBuffer.subSequence(myCurrentTokenStart, myPosition).toString();
    }

    public void advance() {
        if (myPosition >= myEndOffset) {
            myCurrentToken = null;
            return;
        }
        myCurrentTokenStart = myPosition;
        char c = myBuffer.charAt(myPosition);

        if (Character.isWhitespace(c)) {
            advanceOverWhitespace();
            myCurrentToken = LexerTokenType.WHITESPACE;
            while (myPosition < myEndOffset && Character.isWhitespace(myBuffer.charAt(myPosition))) {
                advanceOverWhitespace();
            }
            return;
        } else if (c == '#') { // todo check steps args
            myCurrentToken = LexerTokenType.COMMENT;
            advanceToEOL();
            return;
        } else if (c == '@') {
            myCurrentToken = LexerTokenType.TAG;
            myPosition++;
            while (myPosition < myEndOffset && isValidTagChar(myBuffer.charAt(myPosition))) {
                myPosition++;
            }
            return;
        } else if (c == ':') { // todo refactor
            myCurrentToken = LexerTokenType.COLON;
            myPosition++;
            return;
        } else if (c == '|') {
            myCurrentToken = LexerTokenType.PIPE;
            myPosition++;
            myState = STATE_TABLE;
            return;
        } else if (myState == STATE_TABLE) {
            myCurrentToken = LexerTokenType.TABLE_CELL;
            while (myPosition < myEndOffset && myBuffer.charAt(myPosition) != '|' && myBuffer.charAt(myPosition) != '\n' && myBuffer.charAt(myPosition) != '#') {
                myPosition++;
            }
            while (myPosition > 0 && Character.isWhitespace(myBuffer.charAt(myPosition - 1))) {
                myPosition--;
            }
            return;
        } else if (isStringAtPosition(PYSTRING_MARKER)) {
            myCurrentToken = LexerTokenType.PYSTRING;
            myPosition += 3;
            while (myPosition < myEndOffset && !isStringAtPosition(PYSTRING_MARKER)) {
                myPosition++;
            }
            myPosition += 3;
            return;
        } else if (myState == STATE_DEFAULT) {

            for (String keyword : myKeywords) {

                if (isStringAtPosition(keyword)) {

                    int length = keyword.length();

                    if (myEndOffset - myPosition > length && !Character.isLetterOrDigit(myBuffer.charAt(myPosition + length))) {
                        myPosition += length;
                        myCurrentToken = LexerTokenType.KEYWORDS.get(keyword);

                        if (myCurrentToken == STEP_KEYWORD) {
                            myState = STATE_AFTER_STEP_KEYWORD;
                        } else {
                            myState = STATE_AFTER_KEYWORD;
                        }
                        return;
                    }
                }
            }
        }

        myCurrentToken = LexerTokenType.TEXT;

        advanceToEolOrComment();

    }

    private static boolean isValidTagChar(char c) {
        return !Character.isWhitespace(c) && c != '@';
    }

    private boolean isStringAtPosition(String keyword) {
        int length = keyword.length();
        return myEndOffset - myPosition >= length && myBuffer.subSequence(myPosition, myPosition + length).toString().equals(keyword);
    }

    private void advanceOverWhitespace() {
        if (myBuffer.charAt(myPosition) == '\n') {
            myState = STATE_DEFAULT;
        }
        myPosition++;
    }

    private void advanceToEOL() {
        myPosition++;
        while (myPosition < myEndOffset && myBuffer.charAt(myPosition) != '\n') {
            myPosition++;
        }
        myState = STATE_DEFAULT;
    }

    private void advanceToEolOrComment() {
        myPosition++;
        while (myPosition < myEndOffset && myBuffer.charAt(myPosition) != '\n' && myBuffer.charAt(myPosition) != '#') {
            myPosition++;
        }
        myState = STATE_DEFAULT;
    }


    private static List<String> getKeywords() {
        List<String> keywords = new ArrayList<String>(LexerTokenType.KEYWORDS.keySet());
        Collections.sort(keywords, new Comparator<String>() {
            public int compare(String x0, String x1) {
                return x1.length() - x0.length();
            }
        });
        return keywords;
    }

    public boolean hasNewLine(int start, int end) {
        while (start < end && start < myEndOffset) {
            if (myBuffer.charAt(start++) == '\n') {
                return true;
            }
        }
        return false;
    }
}
