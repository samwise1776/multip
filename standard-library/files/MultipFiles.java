package standardlibrary.files;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Multip Standard Library — Files Module
 * Provides file system operations.
 */
public class MultipFiles {
    public static String read(String path) throws IOException {
        return Files.readString(Path.of(path));
    }
    public static void write(String path, String content) throws IOException {
        Files.writeString(Path.of(path), content);
    }
    public static void append(String path, String content) throws IOException {
        Files.writeString(Path.of(path), content, StandardOpenOption.APPEND);
    }
    public static boolean exists(String path) { return Files.exists(Path.of(path)); }
    public static boolean isFile(String path) { return Files.isRegularFile(Path.of(path)); }
    public static boolean isDir(String path) { return Files.isDirectory(Path.of(path)); }
    public static long size(String path) throws IOException { return Files.size(Path.of(path)); }
    public static void delete(String path) throws IOException { Files.deleteIfExists(Path.of(path)); }
    public static void mkdir(String path) throws IOException { Files.createDirectories(Path.of(path)); }
    public static void copy(String src, String dest) throws IOException { Files.copy(Path.of(src), Path.of(dest), StandardCopyOption.REPLACE_EXISTING); }
    public static void move(String src, String dest) throws IOException { Files.move(Path.of(src), Path.of(dest), StandardCopyOption.REPLACE_EXISTING); }
    public static List<String> list(String path) throws IOException {
        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path))) {
            for (Path entry : stream) result.add(entry.getFileName().toString());
        }
        return result;
    }
    public static List<String> listFiles(String path) throws IOException {
        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path), Files::isRegularFile)) {
            for (Path entry : stream) result.add(entry.getFileName().toString());
        }
        return result;
    }
    public static List<String> listDirs(String path) throws IOException {
        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(path), Files::isDirectory)) {
            for (Path entry : stream) result.add(entry.getFileName().toString());
        }
        return result;
    }
    public static String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : "";
    }
    public static String getFileName(String path) {
        return Path.of(path).getFileName().toString();
    }
    public static String getParent(String path) {
        return Path.of(path).getParent().toString();
    }
    public static String getAbsolutePath(String path) {
        return Path.of(path).toAbsolutePath().toString();
    }
    public static String getTempDir() { return System.getProperty("java.io.tmpdir"); }
    public static String getHomeDir() { return System.getProperty("user.home"); }
    public static String getCwd() { return System.getProperty("user.dir"); }
    public static void watch(String path, Runnable onChange) throws IOException {
        // Simplified: just call onChange once
        onChange.run();
    }
}
