package com.github.dokky.gherkin;

import com.github.dokky.gherkin.parser.FeatureHandler;
import com.github.dokky.gherkin.parser.ModelFeatureHandler;
import com.github.dokky.gherkin.parser.Parser;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class Scanner {

    private final static FileFilter DEFAULT_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().contains(".feature");
        }
    };

    private String readFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();

        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getAbsolutePath(), e);
        }
    }


    private void scanDirectory(File directory, List<File> files) {
        for (File file : directory.listFiles(DEFAULT_FILTER)) {
            if (file.isDirectory()) {
                scanDirectory(file, files);
            } else {
                files.add(file);
            }
        }
    }

    public static void main(String[] args) {
//        FeatureHandler handler = new FeaturePrettyPrinter();
        FeatureHandler handler = new ModelFeatureHandler();
        Parser parser = new Parser(handler);
        Scanner scanner = new Scanner();
        List<File> files = new LinkedList<>();
        scanner.scanDirectory(new File("D:\\projects\\msdp\\bdd\\src\\msdptest"), files);
//        files.add(new File("D:\\projects\\msdp\\bdd\\src\\msdptest\\aaa\\auth_nonce_with_authToken.feature"));
        log.info("files found: " + files.size());
        long start = System.currentTimeMillis();
        for (File file : files) {
            parser.parse(scanner.readFile(file));
        }
        log.info("time: " + (System.currentTimeMillis() - start));

    }


}
