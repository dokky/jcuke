package com.github.dokky.gherkin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
        return scanDirectory(directory, DEFAULT_FILTER);
    }

    public static List<File> scanDirectory(File directory, FileFilter filter) {
        List<File> files = new LinkedList<>();
        scanDirectoryInner(directory, files, filter);
        return files;
    }

    private static void scanDirectoryInner(File directory, List<File> files, FileFilter filter) {
        for (File file : directory.listFiles(filter)) {
            if (file.isDirectory()) {
                scanDirectoryInner(file, files, filter);
            } else {
                files.add(file);
            }
        }
    }

}
