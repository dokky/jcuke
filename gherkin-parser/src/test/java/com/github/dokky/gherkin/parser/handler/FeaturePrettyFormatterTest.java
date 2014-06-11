package com.github.dokky.gherkin.parser.handler;

import com.github.dokky.gherkin.FileUtils;
import com.github.dokky.gherkin.parser.GherkinParser;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class FeaturePrettyFormatterTest {
    @Test
    public void testFile() {
//        String reformatted = reformat(new File("bdd/msdptest/aps/email_events.feature"));
        String reformatted = reformat(new File("/home/stanislavd/workspace/msdp/bdd/src/msdptest/cm/product_revision_rest.feature"));
        System.err.println(reformatted);
    }

    @Test
    public void testDir() {
//        Set<File> files = new TreeSet<>(FileUtils.scanDirectory(new File("/home/stanislavd/workspace/techweb/bdd")));
//        Set<File> files = new TreeSet<>(FileUtils.scanDirectory(new File("/home/stanislavd/workspace/msdp/bdd/src/msdptest")));
//        Set<File> files = new TreeSet<>(FileUtils.scanDirectory(new File("bdd")));
        Set<File> files = new TreeSet<>(FileUtils.scanDirectory(new File("D:\\projects\\msdp\\bdd\\src\\msdptest")));
        int i = 1;
        for (File file : files) {
            System.err.println("["+(i++)+"/"+files.size()+"]:"+file);

            try {
                String reformatted = reformat(file);
//                System.err.println(reformatted);
//                Thread.sleep(5000);
            } catch (Exception|AssertionError e) {
                throw new RuntimeException("file: " + file + " Error: "+ e.getMessage(), e);
            }
        }
    }

    private String reformat(File file) {
        GherkinPrettyFormatterHandler handler = new GherkinPrettyFormatterHandler();
        GherkinParser parser = new GherkinParser(handler);

        String original = FileUtils.readFile(file);
        parser.parse(original);
        String formatted = handler.getResult();
//        System.err.println(formatted);

        String expected = removeWhitespaces(original);
        String actual = removeWhitespaces(formatted);
        Assert.assertEquals("Formatted not equal original: " + diff(original, formatted), expected, actual);
        return formatted;
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
