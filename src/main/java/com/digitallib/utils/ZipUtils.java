package com.digitallib.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void zipFolder(Path sourceFolderPath, Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Only copy files, not directories
                    if (attrs.isSymbolicLink()) {
                        return FileVisitResult.CONTINUE;
                    }

                    Path targetFile = sourceFolderPath.relativize(file);
                    zos.putNextEntry(new ZipEntry(targetFile.toString().replace(File.separator, "/")));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!sourceFolderPath.equals(dir)) {
                        Path targetFile = sourceFolderPath.relativize(dir);
                        zos.putNextEntry(new ZipEntry(targetFile.toString().replace(File.separator, "/") + "/"));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static void unzip(Path zipFilePath, Path destDirectory) throws IOException {
        Path canonicalDest = destDirectory.toAbsolutePath().normalize();
        File destDir = canonicalDest.toFile();
        if (!destDir.exists()) destDir.mkdirs();

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath.toFile()))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                // Guard against Zip Slip: ensure resolved path stays inside destDirectory
                Path resolvedPath = canonicalDest.resolve(entry.getName()).normalize();
                if (!resolvedPath.startsWith(canonicalDest)) {
                    throw new IOException("Zip Slip detected – refusing to extract outside destination: " + entry.getName());
                }

                if (!entry.isDirectory()) {
                    extractFile(zipIn, resolvedPath.toString());
                } else {
                    resolvedPath.toFile().mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read = 0;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
