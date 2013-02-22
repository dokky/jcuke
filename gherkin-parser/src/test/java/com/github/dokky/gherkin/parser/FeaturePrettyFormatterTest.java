package com.github.dokky.gherkin.parser;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import lombok.Data;

import org.junit.Assert;
import org.junit.Test;

import com.github.dokky.gherkin.FileUtils;

public class FeaturePrettyFormatterTest {
    @Test
    public void testFile() {
        formatAndAssert(new File("bdd/msdptest/aps/security.feature"));

    }

    @Test
    public void testDir() {
        Set<File> files = new TreeSet<>(FileUtils.scanDirectory(new File("bdd")));
        int i = 1;
        for (File file : files) {
            System.err.println("["+(i++)+"/"+files.size()+"]:"+file);

            try {
                formatAndAssert(file);
            } catch (Exception e) {
                throw new RuntimeException("file: " + file + " Error: "+ e.getMessage());
            }
        }
    }

    private void formatAndAssert(File file) {
        FeaturePrettyFormatter handler = new FeaturePrettyFormatter();
        Parser parser = new Parser(handler);

        String original = FileUtils.readFile(file);
        parser.parse(original);
        String formatted = handler.getResult();
//        System.err.println(formatted);

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
            char e = eci.next();
            char a = aci.next();
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
                return ("line: " + line + " position: " + i + " " + " char: " + exp[i] + " != " + act[i] + "  context: " + new String(exp, i, 60) + " <> " + new String(act, i,
                                                                                                                                                                        60));
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
            if (i >= chars.length) {
                return false;
            }
            c = chars[i++];
            while (i < chars.length && Character.isWhitespace(c)) {
                if (c == '\n') {
                    line++;
                }
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
