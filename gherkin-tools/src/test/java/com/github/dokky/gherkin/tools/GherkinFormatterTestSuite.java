package com.github.dokky.gherkin.tools;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

public class GherkinFormatterTestSuite extends TestSuite {

    public GherkinFormatterTestSuite() {
        super("formatter");
        System.out.println(FileUtils.getTempDirectory());
        Collection<File> files = FileUtils.listFiles(new File("gherkin-tools/src/test/resources/formatter/valid"), new String[]{"feature"}, true);
        for (File file : files) {
            addTest(new FormatterTest(file, GherkinValidator.ValidationResult.STATUS_OK));
        }
    }

    class FormatterTest extends TestCase {
        private File feature;
        private String expectedStatus;

        public FormatterTest(File feature, String expectedStatus) {
            this.feature = feature;
            this.expectedStatus = expectedStatus;
            setName(feature.getName() + "[" + expectedStatus + "]");
        }

        public void runTest() throws Exception {
            GherkinPrettyFormatter.ReformatResult reformatResult = new GherkinPrettyFormatter().format(feature, new File(FileUtils.getTempDirectory(), feature.getName()));
            Assert.assertEquals(reformatResult.toString(), expectedStatus, reformatResult.getStatus());
            Assert.assertEquals("Reformatted with differences", FileUtils.readFileToString(new File(feature.getParentFile().getParentFile(), "valid.formatted/" + feature.getName())), reformatResult.getFormatted());
        }
    }

}
