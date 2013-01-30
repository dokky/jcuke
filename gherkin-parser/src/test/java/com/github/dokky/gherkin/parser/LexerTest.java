package com.github.dokky.gherkin.parser;

import com.github.dokky.gherkin.lexer.Lexer;
import com.github.dokky.gherkin.lexer.TokenType;
import org.junit.Assert;

public class LexerTest {
    private static void doTest(String text, String[] expectedTokens) {
        Lexer lexer = new Lexer(text);
        doTest(text, expectedTokens, lexer);
    }

    private static void doTest(String text, String expectedTokens) {
        Lexer lexer = new Lexer(text);
        doTest(text, expectedTokens.split(";"), lexer);
    }

    private static void doTest(String text, String[] expectedTokens, Lexer lexer) {
        lexer.parseNextToken();
        int idx = 0;
        while (lexer.getCurrentTokenType() != null) {
            if (lexer.getCurrentTokenType() != TokenType.WHITESPACE) {
                if (idx >= expectedTokens.length) Assert.fail("Too many tokens");
                String expectedTokenType = expectedTokens[idx++];
                String tokenName = lexer.getCurrentTokenType().toString();
                Assert.assertEquals("Token name does not match at position: " + idx, expectedTokenType, tokenName);

                String expectedTokenText = expectedTokens[idx++];
                String tokenText = lexer.getCurrentTokenValue();
                Assert.assertEquals("Token text does not match at position: " + idx, expectedTokenText, tokenText);
            }
            lexer.parseNextToken();
        }

        if (idx < expectedTokens.length) Assert.fail("Not enough tokens");
    }


    @org.junit.Test
    public void testSimple() throws Exception {
        doTest("Feature: XYZ", "FEATURE_KEYWORD;Feature:;TEXT;XYZ");
        doTest("Feature : XYZ", "FEATURE_KEYWORD;Feature :;TEXT;XYZ");
    }

    @org.junit.Test
    public void testText() throws Exception {
        doTest("text", new String[]{"TEXT", "text",});

    }

    @org.junit.Test
    public void testComment1() throws Exception {
        doTest("#", new String[]{"COMMENT", "#",});
        doTest("#comment", new String[]{"COMMENT", "#comment",});
        doTest("#comment#comment", new String[]{"COMMENT", "#comment#comment",});
        doTest("#comment #comment", new String[]{"COMMENT", "#comment #comment",});
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a| #comment", "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a| #comment\n|a|\n", "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment;PIPE;|;TABLE_CELL;a;PIPE;|");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a| #comment\n|a|", "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment;PIPE;|;TABLE_CELL;a;PIPE;|");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a|\n#comment\n|a|", "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment;PIPE;|;TABLE_CELL;a;PIPE;|");
    }

    @org.junit.Test
    public void testTable() throws Exception {
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a|\n|a|", "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;PIPE;|;TABLE_CELL;a;PIPE;|");
    }

    @org.junit.Test
    public void testTag() throws Exception {
        doTest("@tag", new String[]{"TAG", "@tag",});
        doTest("@tag1@tag2", new String[]{"TAG", "@tag1", "TAG", "@tag2",});
        doTest("@tag1 @tag2", new String[]{"TAG", "@tag1", "TAG", "@tag2",});
    }

    @org.junit.Test
    public void testFeature() throws Exception {
        doTest("Feature", new String[]{"TEXT", "Feature",});
        doTest("Feature:", new String[]{"FEATURE_KEYWORD", "Feature:",});
        doTest("Feature :", new String[]{"FEATURE_KEYWORD", "Feature :",});
        doTest("Feature: Title", new String[]{"FEATURE_KEYWORD", "Feature:", "TEXT", "Title",});
        doTest("Feature: Title\nDescription", new String[]{"FEATURE_KEYWORD", "Feature:", "TEXT", "Title", "TEXT", "Description",});
        doTest("Feature: Title\nDescription When", new String[]{"FEATURE_KEYWORD", "Feature:", "TEXT", "Title", "TEXT", "Description When",});
        doTest("Feature: Title\nWhen Description", new String[]{"FEATURE_KEYWORD", "Feature:", "TEXT", "Title", "TEXT", "When Description",});
    }

    @org.junit.Test
    public void testScenario() throws Exception {
        doTest("Feature: XYZ\n" +
               "Background:\n" +
               "Given A\n" +
               "Scenario:\n" +
               "Given B",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "BACKGROUND_KEYWORD;Background:;" +
               "STEP_KEYWORD;Given;TEXT;A;" +
               "SCENARIO_KEYWORD;Scenario:;" +
               "STEP_KEYWORD;Given;TEXT;B;");

        doTest("Feature: XYZ\n" +
               "Background:\n" +
               "Given A\n" +
               "Scenario: name\n" +
               "Given B",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "BACKGROUND_KEYWORD;Background:;" +
               "STEP_KEYWORD;Given;TEXT;A;" +
               "SCENARIO_KEYWORD;Scenario:;TEXT;name;" +
               "STEP_KEYWORD;Given;TEXT;B;");

        doTest("Feature: XYZ\n" +
               "Background:\n" +
               "Given A\n" +
               "Scenario Outline:\n" +
               "Given B",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "BACKGROUND_KEYWORD;Background:;" +
               "STEP_KEYWORD;Given;TEXT;A;" +
               "SCENARIO_OUTLINE_KEYWORD;Scenario Outline:;" +
               "STEP_KEYWORD;Given;TEXT;B;");

        doTest("Feature: XYZ\n" +
               "Background:\n" +
               "Given A\n" +
               "Scenario Outline:\n" +
               "When B",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "BACKGROUND_KEYWORD;Background:;" +
               "STEP_KEYWORD;Given;TEXT;A;" +
               "SCENARIO_OUTLINE_KEYWORD;Scenario Outline:;" +
               "STEP_KEYWORD;When;TEXT;B;");

        doTest("Feature: XYZ\n" +
               "Scenario:\n" +
               "Given B",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "SCENARIO_KEYWORD;Scenario:;" +
               "STEP_KEYWORD;Given;TEXT;B;");

        doTest("Feature: XYZ\n" +
               "Scenario:\n" +
               "Given B\n" +
               "Scenario:\n" +
               "Given B",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "SCENARIO_KEYWORD;Scenario:;" +
               "STEP_KEYWORD;Given;TEXT;B;" +
               "SCENARIO_KEYWORD;Scenario:;" +
               "STEP_KEYWORD;Given;TEXT;B;"
              );

        doTest("Feature: XYZ\n" +
               "Scenario:\n" +
               "Given B\n" +
               "Background:\n" +
               "Given A\n",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "SCENARIO_KEYWORD;Scenario:;" +
               "STEP_KEYWORD;Given;TEXT;B;" +
               "BACKGROUND_KEYWORD;Background:;" +
               "STEP_KEYWORD;Given;TEXT;A;");
    }

    @org.junit.Test
    public void testScenarioOutline() throws Exception {
        doTest("Feature: XYZ\n" +
               "Scenario Outline:\n" +
               "Given B\n" +
               "Examples:\n" +
               "|a|\n",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "SCENARIO_OUTLINE_KEYWORD;Scenario Outline:;" +
               "STEP_KEYWORD;Given;TEXT;B;" +
               "EXAMPLES_KEYWORD;Examples:;" +
               "PIPE;|;TABLE_CELL;a;PIPE;|"
              );

        doTest("Feature: XYZ\n" +
               "Scenario Outline: name\n" +
               "Given B",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;" +
               "SCENARIO_OUTLINE_KEYWORD;Scenario Outline:;TEXT;name;" +
               "STEP_KEYWORD;Given;TEXT;B;");

    }

}
