package com.github.dokky.gherkin.parser;

import org.junit.Assert;
import org.junit.Test;

import com.github.dokky.gherkin.parser.handler.GherkinFilePrettyFormatter;

public class GherkinParserTest {

    public void testParse(String feature, String expected) throws Exception {
//        DebugHandler handler = new DebugHandler();
        GherkinFilePrettyFormatter handler = new GherkinFilePrettyFormatter();
        GherkinParser p = new GherkinParser(handler);
        p.parse(feature);
        String result = handler.getResult();
        System.out.println(result);
        Assert.assertEquals("Parser modified", expected, result);
    }

    public void testException(String feature, Class<? extends Exception> exceptionClass, String message) {
        Throwable t = null;
        try {
            GherkinFilePrettyFormatter handler = new GherkinFilePrettyFormatter();
            GherkinParser p = new GherkinParser(handler);
            p.parse(feature);
            String result = handler.getResult();
            System.out.println(result);
        } catch (Throwable throwable) {
            t = throwable;
        }
        System.out.println(t);
        Assert.assertEquals(exceptionClass, t != null ? t.getClass() : null);
        Assert.assertEquals(message, t != null? t.getMessage(): null);


    }

    @Test
    public void testSimpleTablesNoSpaces() throws Exception {
        testParse("Feature: XYZ\nBackground:\nGiven table:\n|name|\n|value|\n",
                  "Feature: XYZ\n" +
                  "\n" +
                  "    Background: \n" +
                  "        Given table:\n" +
                  "            | name     |\n" +
                  "            | value    |\n");
        testParse("Feature: XYZ\nBackground:\nGiven table:\n|name|\n|value|",
                  "Feature: XYZ\n" +
                  "\n" +
                  "    Background: \n" +
                  "        Given table:\n" +
                  "            | name     |\n" +
                  "            | value    |\n");

        testParse("Feature: XYZ\nBackground:\nGiven table:\n|a|b|\n|a1|b1|",
                  "Feature: XYZ\n" +
                  "\n" +
                  "    Background: \n" +
                  "        Given table:\n" +
                  "            | a    | b    |\n" +
                  "            | a1   | b1   |\n");

    }
    @Test
    public void testSimpleTablesWithSpaces() throws Exception {
        testParse("Feature: XYZ\nBackground:\nGiven table:\n| name |\n| value |\n",
                  "Feature: XYZ\n" +
                  "\n" +
                  "    Background: \n" +
                  "        Given table:\n" +
                  "            | name     |\n" +
                  "            | value    |\n");
        testParse("Feature: XYZ\nBackground:\nGiven table:\n | name | \n | value | ",
                  "Feature: XYZ\n" +
                  "\n" +
                  "    Background: \n" +
                  "        Given table:\n" +
                  "            | name     |\n" +
                  "            | value    |\n");

        testParse("Feature: XYZ\nBackground:\nGiven table:\n |  a  |  b  |  \n  |  a1  |  b1  |",
                  "Feature: XYZ\n" +
                  "\n" +
                  "    Background: \n" +
                  "        Given table:\n" +
                  "            | a    | b    |\n" +
                  "            | a1   | b1   |\n");

    }

    @Test
    public void testTablesWithEmptyValues() throws Exception {
        testParse(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n| a1 | |",

                "Feature: XYZ\n" +
                "\n" +
                "    Background: \n" +
                "        Given table:\n" +
                "            | a    | b    |\n" +
                "            | a1   |      |\n");
        testParse(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n|  | b1 |",

                "Feature: XYZ\n" +
                "\n" +
                "    Background: \n" +
                "        Given table:\n" +
                "            | a    | b    |\n" +
                "            |      | b1   |\n");

        testParse(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n|  |  |",

                "Feature: XYZ\n" +
                "\n" +
                "    Background: \n" +
                "        Given table:\n" +
                "            | a    | b    |\n" +
                "            |      |      |\n");


    }

    @Test
    public void testTablesWithEscapedValues() throws Exception {
        testParse(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n| a1 |a\\#b |",

                "Feature: XYZ\n" +
                "\n" +
                "    Background: \n" +
                "        Given table:\n" +
                "            | a    | b    |\n" +
                "            | a1   | a\\#b |\n");
        testParse(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n| a1 |a\\|b |",

                "Feature: XYZ\n" +
                "\n" +
                "    Background: \n" +
                "        Given table:\n" +
                "            | a    | b    |\n" +
                "            | a1   | a\\|b |\n");


    }

    @Test
    public void testTablesWithWrongValues() throws Exception {
        testException(
                "Feature: XYZ\nBackground:\nGiven table:\n| a | b |\n| a1 |",
                GherkinParseException.class,
                "Parsing error at line 7: row.length != header.length. Details: header: [a, b] row: [a1]");
        testException(
                "Feature: XYZ\nBackground:\nGiven table:\n| a |  |\n| a1 |a |",
                GherkinParseException.class,
                "Parsing error at line 7: Empty header at position: 2");
    }

    @Test
    public void testPyString() throws Exception {
        testParse(
                "Feature: XYZ\n" +
                "Scenario:\n" +
                "When testing pyString:\n" +
                "\"\"\"\n" +
                "pystring\n" +
                "\"\"\"\n" +
                "",

                "Feature: XYZ\n" +
                "\n" +
                "\n" +
                "    Scenario: \n" +
                "        When testing pyString:\n" +
                "        \"\"\"\n" +
                "        pystring\n" +
                "        \"\"\"\n");

        testParse(
                "Feature: XYZ\n" +
                "Scenario:\n" +
                "When testing pyString:\n" +
                "\"\"\"\n" +
                "pystring\n" +
                "\"\"\"" +
                "",

                "Feature: XYZ\n" +
                "\n" +
                "\n" +
                "    Scenario: \n" +
                "        When testing pyString:\n" +
                "        \"\"\"\n" +
                "        pystring\n" +
                "        \"\"\"\n");
        testParse(
                "Feature: XYZ\n" +
                "Scenario:\n" +
                "When testing pyString:\n" +
                "\"\"\"\n" +
                "pystring\n" +
                "\"\"\"    ",

                "Feature: XYZ\n" +
                "\n" +
                "\n" +
                "    Scenario: \n" +
                "        When testing pyString:\n" +
                "        \"\"\"\n" +
                "        pystring\n" +
                "        \"\"\"\n");
        testParse(
                "Feature: XYZ\n" +
                "Scenario:\n" +
                "When testing pyString:\n" +
                "\"\"\"\n" +
                "pystring\n",

                "Feature: XYZ\n" +
                "\n" +
                "\n" +
                "    Scenario: \n" +
                "        When testing pyString:\n" +
                "        \"\"\"\n" +
                "        pystring\n" +
                "        \"\"\"\n");
    }

}
