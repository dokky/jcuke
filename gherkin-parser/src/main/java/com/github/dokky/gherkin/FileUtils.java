package com.github.dokky.gherkin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    private final static FileFilter DEFAULT_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory() || pathname.getName().contains(".feature");
        }
    };

    public static String readFile(String directory, String filename) {
        return readFile(new File(directory, filename));
    }

    public static String readFile(File file) {
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


    public static List<File> scanDirectory(File directory) {
        List<File> files = new LinkedList<>();
        scanDirectory(directory, files);
        return files;
    }

    public static void scanDirectory(File directory, List<File> files) {
        for (File file : directory.listFiles(DEFAULT_FILTER)) {
            if (file.isDirectory()) {
                scanDirectory(file, files);
            } else {
                files.add(file);
            }
        }
    }

}
