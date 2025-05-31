// Place this static helper method in a utility class (e.g., FileUtils.java)
// or replicate it in each model class that needs file I/O.

package com.talon.testing.utils; // Example package for utility

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    private static final String DATA_SUBFOLDER_NAME = "data"; // Name of the subfolder in project root

    /**
     * Gets a File object pointing to a file within a 'data' subfolder
     * located at the root of the project.
     * If createIfNotFound is true, it will attempt to create the file and its
     * parent 'data' directory if they don't exist.
     *
     * @param fileName The name of the data file (e.g., "users.txt", "items.txt").
     * @param createIfNotFound If true, attempts to create the file/directory.
     * @param defaultJsonContent The JSON content to write if the file is newly created and empty
     *                           (e.g., "{}" for maps, "[]" for lists).
     * @return File object or null if creation fails and was requested.
     * @throws IOException if directory creation fails catastrophically.
     */
    public static File getDataFileFromProjectRoot(String fileName, boolean createIfNotFound, String defaultJsonContent) throws IOException {
        String projectRootPath = System.getProperty("user.dir");
        File dataDir = new File(projectRootPath, DATA_SUBFOLDER_NAME);
        File dataFile = new File(dataDir, fileName);

        // System.out.println("Attempting to access/create data file at: " + dataFile.getAbsolutePath());

        if (createIfNotFound) {
            if (!dataDir.exists()) {
                System.out.println("Data directory not found. Creating: " + dataDir.getAbsolutePath());
                if (!dataDir.mkdirs()) {
                    // If mkdirs() fails, it might be a permission issue or invalid path part.
                    // Check if it's a file instead of a directory already.
                    if (dataDir.isFile()){
                        throw new IOException("Cannot create data directory because a file with the same name ('data') exists at project root: " + dataDir.getAbsolutePath());
                    }
                    throw new IOException("Could not create data directory: " + dataDir.getAbsolutePath());
                }
            }

            if (!dataFile.exists()) {
                System.out.println("Data file not found. Creating: " + dataFile.getAbsolutePath());
                try {
                    if (dataFile.createNewFile()) {
                        System.out.println("Created new data file: " + dataFile.getAbsolutePath());
                        // Initialize with default JSON content if newly created and empty
                        if (dataFile.length() == 0 && defaultJsonContent != null) {
                            try (Writer writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
                                writer.write(defaultJsonContent);
                                System.out.println("Initialized " + fileName + " with: " + defaultJsonContent);
                            }
                        }
                    } else {
                        // This state (createNewFile returns false but file didn't exist before) is unusual.
                        // It might mean a race condition or a name collision with something non-file.
                        // For robustness, check if it's a directory.
                        if (dataFile.isDirectory()) {
                             throw new IOException("Cannot create data file because a directory with the same name exists: " + dataFile.getAbsolutePath());
                        }
                        System.err.println("Could not create new file (it might have been created by another process, or is a directory): " + dataFile.getAbsolutePath());
                    }
                } catch (IOException ex) {
                    System.err.println("IOException during file creation for " + dataFile.getAbsolutePath() + ": " + ex.getMessage());
                    throw ex; // Re-throw to indicate failure
                }
            }
        }
        return dataFile;
    }
}