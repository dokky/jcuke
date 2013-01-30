package com.github.dokky.gherkin;

import com.github.dokky.gherkin.parser.FeaturePrettyFormatter;
import com.github.dokky.gherkin.parser.Parser;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ScannerTest {
    @Test
    public void testFile() {
        File file = new File("D:\\projects\\msdp\\bdd\\src\\msdptest\\aaa\\notif\\targeting_with_account_id.feature");
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
        String expected = removeWhitespaces(original);
        String actual = removeWhitespaces(formatted);
        Assert.assertEquals("Formatted not equal original: " + diff(expected, actual), expected, actual);
    }

    private String removeWhitespaces(String original) {
        return original.replaceAll("\\s", "");
    }

    private static String diff(String expected, String actual) {
        char[] o = expected.toCharArray();
        char[] f = actual.toCharArray();
        for (int i = 0; i < o.length; i++) {
            if (o[i] != f[i]) {
                return (new String(o, i - 10, 40) + " <> " + new String(f, i - 10, 40));
            }
        }
        return "";
    }
}
