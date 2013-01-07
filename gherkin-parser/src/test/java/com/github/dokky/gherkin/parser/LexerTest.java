package com.github.dokky.gherkin.parser;

import org.junit.Assert;

public class LexerTest {
    private static void doTest(String text, String[] expectedTokens) {
        Lexer lexer = new Lexer();
        doTest(text, expectedTokens, lexer);
    }

    private static void doTest(String text, String[] expectedTokens, Lexer lexer) {
        lexer.start(text);
        int idx = 0;
        while (lexer.getMyCurrentToken() != null) {
            if (lexer.getMyCurrentToken() != LexerTokenType.WHITESPACE) {
                if (idx >= expectedTokens.length) Assert.fail("Too many tokens");
                String expectedTokenType = expectedTokens[idx++];
                String expectedTokenText = expectedTokens[idx++];
                String tokenName = lexer.getMyCurrentToken().toString();
                Assert.assertEquals("Token name does not match", expectedTokenType, tokenName);
                String tokenText = lexer.getCurrentTokenValue();
                Assert.assertEquals("Token text does not match", expectedTokenText, tokenText);
            }
            lexer.advance();
        }

        if (idx < expectedTokens.length) Assert.fail("Not enough tokens");
    }


    @org.junit.Test
    public void testSimple() throws Exception {
        doTest("Feature: XYZ", new String[]{"FEATURE_KEYWORD", "Feature", "COLON", ":", "TEXT", "XYZ",});
        doTest("Feature : XYZ", new String[]{"FEATURE_KEYWORD", "Feature", "COLON", ":", "TEXT", "XYZ",});
    }

    @org.junit.Test
    public void testText() throws Exception {
        doTest("text", new String[]{"TEXT", "text",});

    }

    @org.junit.Test
    public void testComment1() throws Exception {
        doTest("#comment", new String[]{"COMMENT", "#comment",});
        doTest("#comment#comment", new String[]{"COMMENT", "#comment#comment",});
        doTest("#comment #comment", new String[]{"COMMENT", "#comment #comment",});

    }

    @org.junit.Test
    public void testTag() throws Exception {
        doTest("@tag", new String[]{"TAG", "@tag",});
        doTest("@tag1@tag2", new String[]{"TAG", "@tag1", "TAG", "@tag2",});
        doTest("@tag1 @tag2", new String[]{"TAG", "@tag1", "TAG", "@tag2",});
    }

}
