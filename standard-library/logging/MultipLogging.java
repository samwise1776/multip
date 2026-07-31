package standardlibrary.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Multip Standard Library — Logging Module
 * Provides structured logging.
 */
public class MultipLogging {
    private static String logLevel = "INFO";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void setLevel(String level) { logLevel = level; }
    public static String getLevel() { return logLevel; }

    public static void debug(String message) { log("DEBUG", message); }
    public static void info(String message) { log("INFO", message); }
    public static void warn(String message) { log("WARN", message); }
    public static void error(String message) { log("ERROR", message); }
    public static void fatal(String message) { log("FATAL", message); }

    public static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FMT);
        System.out.println("[" + timestamp + "] [" + level + "] " + message);
    }

    public static void table(String title, String[][] data) {
        System.out.println("=== " + title + " ===");
        for (String[] row : data) {
            StringBuilder sb = new StringBuilder();
            for (String cell : row) sb.append(String.format("%-20s", cell));
            System.out.println(sb);
        }
    }

    public static void separator() {
        System.out.println("─".repeat(50));
    }

    public static void banner(String text) {
        System.out.println("╔" + "═".repeat(text.length() + 2) + "╗");
        System.out.println("║ " + text + " ║");
        System.out.println("╚" + "═".repeat(text.length() + 2) + "╝");
    }
}
