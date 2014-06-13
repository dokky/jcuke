package com.github.dokky.gherkin.tools;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        //$JUnit-BEGIN$
        suite.addTest(new GherkinValidatorTestSuite());
        //$JUnit-END$
        return suite;
    }
}
