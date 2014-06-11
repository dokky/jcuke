package com.github.dokky.gherkin.tools;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.github.dokky.gherkin.parser.GherkinParser;
import com.github.dokky.gherkin.parser.handler.GherkinPrettyFormatterHandler;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


public class GherkinPrettyFormater {


    public void format(File directory, String[] extensions, boolean recursive) {
        Collection<File> files = FileUtils.listFiles(directory, extensions, recursive);
        int i = 1;
        int errors = 0;
        for (File file : files) {
            ValidationResult validationResult = format(file);

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

    }

    private ValidationResult format(File file) {
        ValidationResult result = new ValidationResult(file);

        try {
            String original = FileUtils.readFileToString(file);

            GherkinPrettyFormatterHandler handler = new GherkinPrettyFormatterHandler();
            GherkinParser parser = new GherkinParser(handler);
            parser.parse(original);
            String parsed = handler.getResult();

            if (!removeWhitespaces(original).equals(removeWhitespaces(parsed))) {
                throw new ParseException("Parsed content differs from original", -1);
            }
        } catch (Throwable e) {
            result.status = ValidationResult.STATUS_FAILED;
            result.errorMessages.add(e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private String removeWhitespaces(String original) {
        return original.replaceAll("\\s+", "");
    }


    @Data
    private final static class ValidationResult {
        public final static String STATUS_OK = "OK";
        public final static String STATUS_FAILED = "FAILED";

        final File file;
        String status = STATUS_OK;
        List<String> errorMessages = new LinkedList<>();


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

        GherkinPrettyFormater validator = new GherkinPrettyFormater();
        validator.format(new File(parameters.dir), parameters.extensions, parameters.recursive);
    }
}
