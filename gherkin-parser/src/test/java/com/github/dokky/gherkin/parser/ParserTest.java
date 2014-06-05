package com.github.dokky.gherkin.parser;

import org.junit.Assert;

public class ParserTest {

    public void testParse(String feature, String expected) throws Exception {
//        DebugHandler handler = new DebugHandler();
        FeaturePrettyFormatter handler = new FeaturePrettyFormatter();
        Parser p = new Parser(handler);
        p.parse(feature);
        String result = handler.getResult();
        System.out.println(result);
        Assert.assertEquals("Parser modified", expected, result);
    }

    @org.junit.Test
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
    @org.junit.Test
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

    @org.junit.Test
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

}
