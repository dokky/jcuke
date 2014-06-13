package com.github.dokky.gherkin.tools;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;

public class GherkinValidatorTestSuite extends TestSuite {

    public GherkinValidatorTestSuite() {
        super("validator");
        Collection<File> validFiles = FileUtils.listFiles(new File("gherkin-tools/src/test/resources/validator/valid"), new String[]{"feature"}, true);
        for (File validFile : validFiles) {
            addTest(new ValidatorTest(validFile, GherkinValidator.ValidationResult.STATUS_OK));
        }
        Collection<File> invalidFiles = FileUtils.listFiles(new File("gherkin-tools/src/test/resources/validator/invalid"), new String[]{"feature"}, true);
        for (File invalidFile : invalidFiles) {
            addTest(new ValidatorTest(invalidFile, GherkinValidator.ValidationResult.STATUS_FAILED));
        }
    }

    class ValidatorTest extends TestCase {
        private File feature;
        private String expectedStatus;

        public ValidatorTest(File feature, String expectedStatus) {
            this.feature = feature;
            this.expectedStatus = expectedStatus;
            setName(feature.getName() + "[" + expectedStatus + "]");
        }

        public void runTest() throws Exception {
            GherkinValidator.ValidationResult validationResult = new GherkinValidator().validate(feature);
            Assert.assertEquals(validationResult.toString(), expectedStatus, validationResult.getStatus());
        }
    }


//    @Test
//    public void testValidateDirectory() throws Exception {
//        new GherkinValidator().validate(new File("gherkin-tools/src/test/resources/formatter/input"), new String[]{"feature"}, true);
//    }
}
