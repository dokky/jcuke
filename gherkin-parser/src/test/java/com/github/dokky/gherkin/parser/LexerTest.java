package com.github.dokky.gherkin.parser;

import org.junit.Assert;

import com.github.dokky.gherkin.FileUtils;
import com.github.dokky.gherkin.lexer.Lexer;
import com.github.dokky.gherkin.lexer.TokenType;

public class LexerTest {
    final static String directory = "gherkin-parser/src/test/resources/lexer";


    private static void doTest(String text, String expectedTokens) {
        doTest(text, expectedTokens,";");
    }

    private static void doTest2(String text, String expectedTokens) {
        doTest2(text, expectedTokens.split("\n"));
    }


    private static void doTest(String text, String expectedTokensAndValues, String delimiter) {
        String[] expectedTokens = expectedTokensAndValues.split(delimiter);
        Lexer lexer = new Lexer(text);
        lexer.parseNextToken();
        int idx = 0;
        while (lexer.getCurrentTokenType() != null) {
            if (lexer.getCurrentTokenType() != TokenType.WHITESPACE) {
                if (idx >= expectedTokens.length) {
                    Assert.fail("Too many tokens");
                }
                String expectedTokenType = expectedTokens[idx++];
                String tokenName = lexer.getCurrentTokenType().toString();
                Assert.assertEquals("Token name does not match at position: " + idx, expectedTokenType, tokenName);

                String expectedTokenText = expectedTokens[idx++];
                String tokenText = lexer.getCurrentTokenValue();
                Assert.assertEquals("Token text does not match at position: " + idx, expectedTokenText, tokenText);
            }
            lexer.parseNextToken();
        }

        if (idx < expectedTokens.length) {
            Assert.fail("Not enough tokens");
        }
    }

    private static void doTestWithFullComparison(String text, String expectedTokensAndValues) {
        Lexer lexer = new Lexer(text);
        lexer.parseNextToken();
        StringBuilder actualTokensAndValues = new StringBuilder();
        while (lexer.getCurrentTokenType() != null) {
                String tokenName = lexer.getCurrentTokenType().toString();
                String tokenText = lexer.getCurrentTokenValue();
                actualTokensAndValues.append(tokenName).append('=').append(tokenText).append(';');


                lexer.parseNextToken();
        }
        Assert.assertEquals("Token and values do not match", expectedTokensAndValues, actualTokensAndValues.toString());
    }

    private static void doTest2(String text, String[] expectedTokens) {
        Lexer lexer = new Lexer(text);
        lexer.parseNextToken();
        int line = 0;
        while (lexer.getCurrentTokenType() != null) {
            if (lexer.getCurrentTokenType() != TokenType.WHITESPACE) {

                String expected = expectedTokens[line++];
                String actual = lexer.getCurrentTokenType().toString() + "=" + lexer.getCurrentTokenValue();
                Assert.assertEquals("Token name does not match at line: " + line, expected, actual);

            }
            lexer.parseNextToken();
        }

        if (line < expectedTokens.length) {
            Assert.fail("Not enough tokens");
        }
    }

    private static String breakDownToTokens(String text) {
        StringBuilder out = new StringBuilder();
        Lexer lexer = new Lexer(text);
        lexer.parseNextToken();
        while (lexer.getCurrentTokenType() != null) {
            if (lexer.getCurrentTokenType() != TokenType.WHITESPACE) {
                out.append(lexer.getCurrentTokenType().toString());
                out.append("=");
                out.append(lexer.getCurrentTokenValue());
                out.append("\n");
            }
            lexer.parseNextToken();
        }

        return out.toString();
    }


    @org.junit.Test
    public void testSimple() throws Exception {
        doTest("Feature: XYZ", "FEATURE_KEYWORD;Feature:;TEXT;XYZ");
        doTest("Feature : XYZ", "FEATURE_KEYWORD;Feature :;TEXT;XYZ");
    }

    @org.junit.Test
    public void testText() throws Exception {
        doTest("text", "TEXT;text");

    }

    @org.junit.Test
    public void testComment1() throws Exception {
        doTest("#", "COMMENT;#");
        doTest("#comment", "COMMENT;#comment");
        doTest("#comment#comment", "COMMENT;#comment#comment");
        doTest("#comment #comment", "COMMENT;#comment #comment");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a| #comment",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a| #comment\n|a|\n",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment;PIPE;|;TABLE_CELL;a;PIPE;|");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a| #comment\n|a|",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment;PIPE;|;TABLE_CELL;a;PIPE;|");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a|\n#comment\n|a|",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;COMMENT;#comment;PIPE;|;TABLE_CELL;a;PIPE;|");
    }

    @org.junit.Test
    public void testTable() throws Exception {
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a|\n|a|",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;PIPE;|;TABLE_CELL;a;PIPE;|");
        doTest("Feature: XYZ\nBackground:\nGiven table:\n|a|b|\n|a1|b1|",
               "FEATURE_KEYWORD;Feature:;TEXT;XYZ;BACKGROUND_KEYWORD;Background:;STEP_KEYWORD;Given;TEXT;table:;PIPE;|;TABLE_CELL;a;PIPE;|;TABLE_CELL;b;PIPE;|;PIPE;|;TABLE_CELL;a1;PIPE;|;TABLE_CELL;b1;PIPE;|");
        doTestWithFullComparison(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n| a1 | |",
               "FEATURE_KEYWORD=Feature:;WHITESPACE= ;TEXT=XYZ;WHITESPACE=\n" +
               ";BACKGROUND_KEYWORD=Background:;WHITESPACE=\n" +
               ";STEP_KEYWORD=Given;WHITESPACE= ;TEXT=table:;WHITESPACE=\n" +
               ";PIPE=|;WHITESPACE= ;TABLE_CELL=a;WHITESPACE= ;PIPE=|;WHITESPACE= ;TABLE_CELL=b;WHITESPACE= ;PIPE=|;WHITESPACE=\n" +
               ";PIPE=|;WHITESPACE= ;TABLE_CELL=a1;WHITESPACE= ;PIPE=|;WHITESPACE= ;PIPE=|;");
        doTestWithFullComparison(
                "Feature: XYZ\nBackground:\nGiven table:\n| | b |\n| a1 | |",
               "FEATURE_KEYWORD=Feature:;WHITESPACE= ;TEXT=XYZ;WHITESPACE=\n" +
               ";BACKGROUND_KEYWORD=Background:;WHITESPACE=\n" +
               ";STEP_KEYWORD=Given;WHITESPACE= ;TEXT=table:;WHITESPACE=\n" +
               ";PIPE=|;WHITESPACE= ;PIPE=|;WHITESPACE= ;TABLE_CELL=b;WHITESPACE= ;PIPE=|;WHITESPACE=\n" +
               ";PIPE=|;WHITESPACE= ;TABLE_CELL=a1;WHITESPACE= ;PIPE=|;WHITESPACE= ;PIPE=|;");
        doTestWithFullComparison(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n| a1 ||",
                "FEATURE_KEYWORD=Feature:;WHITESPACE= ;TEXT=XYZ;WHITESPACE=\n" +
                ";BACKGROUND_KEYWORD=Background:;WHITESPACE=\n" +
                ";STEP_KEYWORD=Given;WHITESPACE= ;TEXT=table:;WHITESPACE=\n" +
                ";PIPE=|;WHITESPACE= ;TABLE_CELL=a;WHITESPACE= ;PIPE=|;WHITESPACE= ;TABLE_CELL=b;WHITESPACE= ;PIPE=|;WHITESPACE=\n" +
                ";PIPE=|;WHITESPACE= ;TABLE_CELL=a1;WHITESPACE= ;PIPE=|;PIPE=|;");
        doTestWithFullComparison(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n|| a1 |",
                "FEATURE_KEYWORD=Feature:;WHITESPACE= ;TEXT=XYZ;WHITESPACE=\n" +
                ";BACKGROUND_KEYWORD=Background:;WHITESPACE=\n" +
                ";STEP_KEYWORD=Given;WHITESPACE= ;TEXT=table:;WHITESPACE=\n" +
                ";PIPE=|;WHITESPACE= ;TABLE_CELL=a;WHITESPACE= ;PIPE=|;WHITESPACE= ;TABLE_CELL=b;WHITESPACE= ;PIPE=|;WHITESPACE=\n" +
                ";PIPE=|;PIPE=|;WHITESPACE= ;TABLE_CELL=a1;WHITESPACE= ;PIPE=|;");

    }

    @org.junit.Test
    public void testTag() throws Exception {
        doTest("@tag", "TAG;@tag");
        doTest("@tag1@tag2", "TAG;@tag1;TAG;@tag2");
        doTest("@tag1 @tag2", "TAG;@tag1;TAG;@tag2");
    }

    @org.junit.Test
    public void testFeature() throws Exception {
        doTest("Feature", "TEXT;Feature");
        doTest("Feature:", "FEATURE_KEYWORD;Feature:");
        doTest("Feature :", "FEATURE_KEYWORD;Feature :");
        doTest("Feature: Title", "FEATURE_KEYWORD;Feature:;TEXT;Title");
        doTest("Feature: Title\nDescription", "FEATURE_KEYWORD;Feature:;TEXT;Title;TEXT;Description");
        doTest("Feature: Title\nDescription When", "FEATURE_KEYWORD;Feature:;TEXT;Title;TEXT;Description When");
        doTest("Feature: Title\nWhen Description", "FEATURE_KEYWORD;Feature:;TEXT;Title;TEXT;When Description");
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

    @org.junit.Test
    public void testTableAndTags() throws Exception {
        readFeatureFileFromFileAndMatchTokens("table_and_tags");
    }

    private void readFeatureFileFromFileAndMatchTokens(String name) {
        doTest2(FileUtils.readFile(directory, name + ".feature"), FileUtils.readFile(directory, name + ".tokens"));
    }

//    public static void main(String[] args) {
//
//        System.out.println(breakDownToTokens(FileUtils.readFile(directory, "table_and_tags.feature")));
//    }

}
