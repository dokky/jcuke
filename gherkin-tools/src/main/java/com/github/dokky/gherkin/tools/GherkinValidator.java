package com.github.dokky.gherkin.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.dokky.gherkin.model.Feature;
import com.github.dokky.gherkin.model.FeatureFile;
import com.github.dokky.gherkin.model.Scenario;
import com.github.dokky.gherkin.model.ScenarioOutline;
import com.github.dokky.gherkin.parser.GherkinParser;
import com.github.dokky.gherkin.parser.handler.GherkinModelParserHandler;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class GherkinValidator {


    public void validate(File directory, String[] extensions, boolean recursive) {
        Collection<File> files = FileUtils.listFiles(directory, extensions, recursive);
        int i = 1;
        int errors = 0;
        Statistics stats = new Statistics();
        for (File file : files) {

            ValidationResult validationResult = validate(file);
            updateStatistics(validationResult.featureFile, stats);

            if (validationResult.status == ValidationResult.STATUS_FAILED) {
                errors++;
                System.err.println("[" + (i++) + "/" + files.size() + "]:" + validationResult);
            } else {
                System.out.println("[" + (i++) + "/" + files.size() + "]:" + validationResult);
            }
        }
        if (errors > 0) {
            System.err.println("Errors: " + errors);
        } else {
            System.out.println("No errors found");
        }
        System.out.println("Statistics:");
        System.out.println("Features: " + stats.features);
        System.out.println("Unique Scenarios: " + stats.uniqueScenarios);
        System.out.println("Total Scenarios With Examples: " + stats.totalScenarios);
        System.out.println("Total Scenarios Steps With Examples: " + stats.steps);
        System.out.println("Total Background Steps With Examples: " + stats.backgroundSteps);

    }

    private ValidationResult validate(File file) {
        ValidationResult result = new ValidationResult(file);

        try {
            String original = FileUtils.readFileToString(file);

            GherkinModelParserHandler handler = new GherkinModelParserHandler();
            GherkinParser parser = new GherkinParser(handler);
            parser.parse(original);
            FeatureFile featureFile = handler.getFeatureFile();
            result.featureFile = featureFile;


        } catch (Throwable e) {
            result.status = ValidationResult.STATUS_FAILED;
            result.errorMessages.add(e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private void updateStatistics(FeatureFile featureFile, Statistics stats) {
        Feature feature = featureFile.getFeature();

        stats.features++;
        stats.uniqueScenarios += feature.getScenarios().size();

        int totalScenariosInFeature = 0;
        for (Scenario scenario : feature.getScenarios()) {
            if (scenario instanceof ScenarioOutline) {
                ScenarioOutline scenarioOutline = (ScenarioOutline) scenario;
                int examplesCount = scenarioOutline.getExamples().getTable().getRows().size();
                stats.totalScenarios += examplesCount;
                totalScenariosInFeature += examplesCount;
                stats.steps += scenario.getSteps().size() + examplesCount;
            } else {
                stats.totalScenarios++;
                totalScenariosInFeature++;
                stats.steps += scenario.getSteps().size();
            }
        }

        if (feature.getBackground() != null) {
            stats.backgroundSteps += feature.getBackground().getSteps().size() * totalScenariosInFeature;
        }
    }

    @Data
    private final static class ValidationResult {
        public final static String STATUS_OK = "OK";
        public final static String STATUS_FAILED = "FAILED";

        final File file;
        String status = STATUS_OK;
        List<String> errorMessages = new LinkedList<>();
        FeatureFile featureFile;


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(file.getAbsolutePath()).append(": ").append(status);
            if (!errorMessages.isEmpty()) {
                sb.append("\nErrors:");
                for (String errorMessage : errorMessages) {
                    sb.append("\n - ").append(errorMessage);
                }
            }
            return sb.toString();
        }
    }

    private final static class Statistics {
        int features;
        int uniqueScenarios;
        int totalScenarios;
        int steps;
        int backgroundSteps;
    }

    public static void main(String[] args) {


        class Parameters {
            @Parameter(names = "-dir", required = true, echoInput = true)
            String dir;

            @Parameter(names = "-extensions", required = false, echoInput = true, description = "Gherkin file extensions. Default: feature")
            String[] extensions = {"feature"};

            @Parameter(names = "-recursive", required = false, echoInput = true, description = "Include subdirectories. Default: true")
            boolean recursive = true;

        }

        Parameters parameters = new Parameters();

        JCommander jCommander = new JCommander(parameters, args);

        GherkinValidator validator = new GherkinValidator();
        validator.validate(new File(parameters.dir), parameters.extensions, parameters.recursive);
    }
}
