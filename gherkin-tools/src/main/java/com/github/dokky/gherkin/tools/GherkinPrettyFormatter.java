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


public class GherkinPrettyFormatter {


    public void format(File inputDirectory, File outputDirectory, String[] extensions, boolean recursive) {
        if (!outputDirectory.isDirectory()) {
            throw new IllegalArgumentException("Parameter 'outputDirectory' is not a directory");
        }

        Collection<File> files = FileUtils.listFiles(inputDirectory, extensions, recursive);
        int i = 1;
        int errors = 0;
        for (File file : files) {
            ReformatResult reformatResult = format(file, getOutputFile(inputDirectory, file, outputDirectory));

            if (reformatResult.status == ReformatResult.STATUS_FAILED) {
                errors++;
                System.err.println("[" + (i++) + "/" + files.size() + "]:" + reformatResult);
            } else {
                System.out.println("[" + (i++) + "/" + files.size() + "]:" + reformatResult);
            }
        }
        if (errors > 0) {
            System.err.println("Errors: " + errors);
        } else {
            System.out.println("No errors found");
        }

    }

    ReformatResult format(File inputFile, File outputFile) {
        ReformatResult result = new ReformatResult(inputFile);

        try {
            String original = FileUtils.readFileToString(inputFile);

            GherkinPrettyFormatterHandler handler = new GherkinPrettyFormatterHandler();
            GherkinParser parser = new GherkinParser(handler);
            parser.parse(original);
            String formatted = handler.getResult();

            if (!removeWhitespaces(original).equals(removeWhitespaces(formatted))) {
                throw new ParseException("Parsed content differs from original", -1);
            }
            result.formatted = formatted;
            FileUtils.write(outputFile, formatted);
        } catch (Throwable e) {
            result.status = ReformatResult.STATUS_FAILED;
            result.errorMessages.add(e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private String removeWhitespaces(String original) {
        return original.replaceAll("\\s+", "");
    }

    private static File getOutputFile(File inputDirectory, File inputFile, File outputDirectory) {
        String relative = inputDirectory.toURI().relativize(inputFile.toURI()).getPath();
        return new File(outputDirectory, relative);
    }

    @Data
    final static class ReformatResult {
        public final static String STATUS_OK = "OK";
        public final static String STATUS_FAILED = "FAILED";

        final File file;
        String status = STATUS_OK;
        List<String> errorMessages = new LinkedList<>();
        String formatted;


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

            @Parameter(names = "-out", required = true, echoInput = true)
            String out;

            @Parameter(names = "-extensions", required = false, echoInput = true, description = "Gherkin file extensions. Default: feature")
            String[] extensions = {"feature"};

            @Parameter(names = "-recursive", required = false, echoInput = true, description = "Include subdirectories. Default: true")
            boolean recursive = true;

        }

        Parameters parameters = new Parameters();

        JCommander jCommander = new JCommander(parameters, args);

        GherkinPrettyFormatter validator = new GherkinPrettyFormatter();
        validator.format(new File(parameters.dir), new File(parameters.out), parameters.extensions, parameters.recursive);
    }
}
