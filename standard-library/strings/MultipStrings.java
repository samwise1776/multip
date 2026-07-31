package standardlibrary.strings;

/**
 * Multip Standard Library — Strings Module
 * Provides string manipulation functions.
 */
public class MultipStrings {
    public static int length(String s) { return s.length(); }
    public static String upper(String s) { return s.toUpperCase(); }
    public static String lower(String s) { return s.toLowerCase(); }
    public static String trim(String s) { return s.trim(); }
    public static String substring(String s, int start) { return s.substring(start); }
    public static String substring(String s, int start, int end) { return s.substring(start, end); }
    public static int indexOf(String s, String sub) { return s.indexOf(sub); }
    public static int lastIndexOf(String s, String sub) { return s.lastIndexOf(sub); }
    public static boolean contains(String s, String sub) { return s.contains(sub); }
    public static boolean startsWith(String s, String prefix) { return s.startsWith(prefix); }
    public static boolean endsWith(String s, String suffix) { return s.endsWith(suffix); }
    public static String replace(String s, String old, String replacement) { return s.replace(old, replacement); }
    public static String[] split(String s, String delimiter) { return s.split(delimiter); }
    public static String join(String[] parts, String delimiter) { return String.join(delimiter, parts); }
    public static String reverse(String s) { return new StringBuilder(s).reverse().toString(); }
    public static String repeat(String s, int count) { return s.repeat(count); }
    public static String padStart(String s, int length, char padChar) { return s.length() >= length ? s : String.valueOf(padChar).repeat(length - s.length()) + s; }
    public static String padEnd(String s, int length, char padChar) { return s.length() >= length ? s : s + String.valueOf(padChar).repeat(length - s.length()); }
    public static boolean isEmpty(String s) { return s == null || s.isEmpty(); }
    public static boolean isBlank(String s) { return s == null || s.isBlank(); }
    public static int parseInt(String s) { return Integer.parseInt(s); }
    public static double parseDouble(String s) { return Double.parseDouble(s); }
    public static String valueOf(double d) { return String.valueOf(d); }
    public static String valueOf(int i) { return String.valueOf(i); }
    public static String valueOf(boolean b) { return String.valueOf(b); }
    public static String capitalize(String s) { return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1); }
    public static String decapitalize(String s) { return s.isEmpty() ? s : Character.toLowerCase(s.charAt(0)) + s.substring(1); }
    public static String[] lines(String s) { return s.split("\\r?\\n"); }
    public static String concat(String... parts) { return String.join("", parts); }
    public static boolean matches(String s, String regex) { return s.matches(regex); }
    public static String strip(String s) { return s.strip(); }
}
