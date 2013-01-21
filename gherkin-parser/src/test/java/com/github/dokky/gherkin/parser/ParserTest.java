package com.github.dokky.gherkin.parser;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {
    @Test
    public void testParse() throws Exception {
        DebugHandler handler = new DebugHandler();
        Parser p = new Parser(handler);
        String feature = "Using step definitions from: '../steps'\n" +
                         "\n" +
                         "@tag1 @tag2 @6125\n" +
                         "Feature: Super API Feature\n" +
                         "    As the XYZ application\n" +
                         "    I want to be able to use API\n" +
                         "    Using a REST service\n" +
                         "\n" +
                         "    Background:\n" +
                         "        Given XYZS application\n" +
                         "        Given XYZ2 application\n" +
                         "        And table1:\n" +
                         "            | table1_id |\n" +
                         "            | 123       |\n" +
                         "            | 456       |\n" +
                         "        And table2:\n" +
                         "            | table1_id | table2_id |\n" +
                         "            | 123       | 1         |\n" +
                         "            | 456       | a@b.cd    |\n" +
                         "        And I commit\n" +
                         "\n" +
                         "\n" +
                         "    #Comment\n" +
                         "    @6324 @6804\n" +
                         "    Scenario: MyScenario 1\n" +
                         "        Given ABC application\n" +
                         "        When as user 'admin' I send a GET to '/api?startDate=2001-01-01&endDate=2001-01-01&a@b.cd'\n" +
                         "        And I do activity X\n" +
                         "        Then I expect HTTP code 404\n" +
                         "        And the 'X-Status' header value is 'a@b.cd'\n" +
                         "        And multiline step\n" +
                         "        \"\"\"\n" +
                         "        line 1\n" +
                         "        line 2\n" +
                         "        \"\"\"\n" +
                         "        #comment @25335\n" +
                         "        Then I expect account 'user1' with fields:\n" +
                         "            | field\t\t\t\t\t| value \t\t|\n" +
                         "            | AutoPay\t\t\t\t| True\t\t\t|\n" +
                         "            | Status\t\t\t\t| Active\t\t|\n" +
                         "\n" +
                         "\n" +
                         "    Scenario Outline: MyScenario Outline1\n" +
                         "        When as user 'admin' I send a GET to '/api?startDate=2001-01-01&endDate=2001-01-01'\n" +
                         "        Then I expect HTTP code 404    #comment\n" +
                         "        And the 'X-MSDP-Status' header value is 'UNIT_NOT_FOUND'\n" +
                         "\n" +
                         "        Examples:\n" +
                         "            | val1  | val1      | # comment\n" +
                         "            | 123   | 1         |\n" +
                         "            | 123   | a@b.cd    |\n" +
                         "\n" +
                         "\n" +
                         "    Scenario: MyScenario X\n" +
                         "        When as user 'admin' I send a GET to '/api?startDate=2001-01-01&endDate=2001-01-01&a@b.cd'\n" +
                         "        Then skip\n";
        p.parse(feature);
        String result = handler.getResult();
        System.out.println(result);
        String expected = "[Using step definitions from: '../steps'][\n" +
                          "\n" +
                          "][@tag1][ ][@tag2][ ][@6125][\n" +
                          "][Feature:][Super API Feature]\n" +
                          "[As the XYZ application\n" +
                          "I want to be able to use API\n" +
                          "Using a REST service]\n" +
                          "[\n" +
                          "\n" +
                          "    ][Background:][null]\n" +
                          "[Given][XYZS application]\n" +
                          "[Given][XYZ2 application]\n" +
                          "[And][table1:]\n" +
                          "[ ][|][table1_id][|]\n" +
                          "[|][123][|]\n" +
                          "[|][456][|]\n" +
                          "[\n" +
                          "        ][And][table2:]\n" +
                          "[ ][|][table1_id][|][table2_id][|]\n" +
                          "[|][123][|][1][|]\n" +
                          "[|][456][|][a@b.cd][|]\n" +
                          "[\n" +
                          "        ][And][I commit]\n" +
                          "[#Comment][\n" +
                          "    ][@6324][ ][@6804][\n" +
                          "    ][Scenario:][MyScenario 1]\n" +
                          "[Given][ABC application]\n" +
                          "[When][as user 'admin' I send a GET to '/api?startDate=2001-01-01&endDate=2001-01-01&a@b.cd']\n" +
                          "[And][I do activity X]\n" +
                          "[Then][I expect HTTP code 404]\n" +
                          "[And][the 'X-Status' header value is 'a@b.cd']\n" +
                          "[And][multiline step]\n" +
                          "[\"\"\"][\"\"\"\n" +
                          "        line 1\n" +
                          "        line 2\n" +
                          "        \"\"\"][\"\"\"]\n" +
                          "[\n" +
                          "        ][#comment @25335][\n" +
                          "        ][Then][I expect account 'user1' with fields:]\n" +
                          "[ ][|][field][|][value][|]\n" +
                          "[|][AutoPay][|][True][|]\n" +
                          "[|][Status][|][Active][|]\n" +
                          "[\n" +
                          "\n" +
                          "\n" +
                          "    ][Scenario Outline:][MyScenario Outline1]\n" +
                          "[When][as user 'admin' I send a GET to '/api?startDate=2001-01-01&endDate=2001-01-01']\n" +
                          "[Then][I expect HTTP code 404    ]\n" +
                          "[\n" +
                          "        ][And][the 'X-MSDP-Status' header value is 'UNIT_NOT_FOUND']\n" +
                          "[Examples:][null]\n" +
                          "[ ][ ][# comment][\n" +
                          "            ][ ][|][123][|][1][|]\n" +
                          "[|][123][|][a@b.cd][|]\n" +
                          "[\n" +
                          "\n" +
                          "\n" +
                          "    ][Scenario:][MyScenario X]\n" +
                          "[When][as user 'admin' I send a GET to '/api?startDate=2001-01-01&endDate=2001-01-01&a@b.cd']\n" +
                          "[Then][skip]\n";
        Assert.assertEquals("Parser modified", expected, result);
    }
}
