package com.digitallib.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class RobustFileDeleter {

    private static final Logger logger = LogManager.getLogger(RobustFileDeleter.class);
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_DELAY_MS = 200;

    /**
     * Deletes a file or directory recursively with retries and permission fixes.
     * @param path Path to delete
     * @throws IOException if deletion fails after retries
     */
    public static void delete(Path path) throws IOException {
        if (path == null || !Files.exists(path)) return;

        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    deleteFileWithRetry(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) throw exc;
                    deleteFileWithRetry(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            deleteFileWithRetry(path);
        }
    }

    /**
     * Deletes a file or directory recursively with retries and permission fixes.
     * @param file File to delete
     * @throws IOException if deletion fails after retries
     */
    public static void delete(File file) throws IOException {
        if (file != null) {
            delete(file.toPath());
        }
    }

    private static void deleteFileWithRetry(Path file) throws IOException {
        if (!Files.exists(file)) return;

        // Attempt to make file writable if it's not
        // This handles cases where file is read-only (Windows attribute or permissions)
        try {
            if (!Files.isWritable(file)) {
                file.toFile().setWritable(true);
            }
        } catch (Exception e) {
            // best effort
            logger.debug("Failed to set writable permission for {}", file, e);
        }

        IOException lastException = null;
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                Files.deleteIfExists(file);
                return; // Success
            } catch (java.nio.file.FileSystemException e) {
                lastException = e;
                logger.warn("Attempt {}/{} failed to delete {}: {}", i+1, MAX_RETRIES, file, e.getMessage());

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }

                // On Windows, sometimes System.gc() helps release file handles held by MappedByteBuffers
                if (i == MAX_RETRIES - 2) {
                    System.gc();
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            } catch (IOException e) {
                 // Other IOExceptions might not be temporary, but we can try
                 lastException = e;
            }
        }

        if (lastException != null) {
            throw lastException;
        }
    }
}
