package com.github.dokky.gherkin;

import com.github.dokky.gherkin.parser.FeaturePrettyFormatter;
import com.github.dokky.gherkin.parser.Parser;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ScannerTest {
    @Test
    public void testFile() {
        File file = new File("D:\\projects\\msdp\\bdd\\src\\msdptest\\aps\\security.feature");
        formatAndAssert(file);

    }

    @Test
    public void testDir() {
        Scanner scanner = new Scanner();
        List<File> files = new LinkedList<>();
        scanner.scanDirectory(new File("D:\\projects\\msdp\\bdd\\src\\msdptest"), files);
        for (File file : files) {
            formatAndAssert(file);
        }

    }

    private void formatAndAssert(File file) {
        System.err.println(file);
        FeaturePrettyFormatter handler = new FeaturePrettyFormatter();
        Parser parser = new Parser(handler);
        Scanner scanner = new Scanner();

        String original = scanner.readFile(file);
        parser.parse(original);
        String formatted = handler.getResult();
        System.err.println(formatted);

        String expected = removeWhitespaces(original);
        String actual = removeWhitespaces(formatted);
        Assert.assertEquals("Formatted not equal original: " + diff(original, formatted), expected, actual);
    }

    private String removeWhitespaces(String original) {
        return original.replaceAll("\\s+", "");
    }

    private String diff(String expected, String actual) {
        CharIterator eci = new CharIterator(expected.toCharArray());
        CharIterator aci = new CharIterator(actual.toCharArray());

        int line = 0;
        while (eci.hasNext() && aci.hasNext()) {
            char e = eci.next().charValue();
            char a = aci.next().charValue();
            if (e != a) {
                line = eci.line + 1;
                break;
            }
        }


        expected = removeWhitespaces(expected);
        actual = removeWhitespaces(actual);
        char[] exp = expected.toCharArray();
        char[] act = actual.toCharArray();
        for (int i = 0; i < exp.length; i++) {
            if (exp[i] != act[i]) {
                return ("line: " + line + " position: " + i + " " + " char: " + exp[i] + " != " + act[i] + "  context: " + new String(exp, i, 60) + " <> " + new String(act, i, 60));
            }
        }
        return "";
    }

    @Data
    static class CharIterator implements Iterator<Character> {
        int line;
        final char chars[];
        int  i;
        char c;

        @Override
        public boolean hasNext() {
            if (i >= chars.length)
                return false;
            c = chars[i++];
            while (i < chars.length && Character.isWhitespace(c)) {
                if (c == '\n') line++;
                c = chars[i++];
            }
            return !Character.isWhitespace(c);
        }

        @Override
        public Character next() {
            return c;
        }

        @Override
        public void remove() {
        }
    }
}
