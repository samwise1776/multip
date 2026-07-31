package standardlibrary.math;

/**
 * Multip Standard Library — Math Module
 * Provides mathematical functions and constants.
 */
public class MultipMath {
    public static final double PI = Math.PI;
    public static final double E = Math.E;
    public static final double TAU = Math.PI * 2;
    public static final double SQRT2 = Math.sqrt(2);

    public static double abs(double x) { return Math.abs(x); }
    public static double floor(double x) { return Math.floor(x); }
    public static double ceil(double x) { return Math.ceil(x); }
    public static double round(double x) { return Math.round(x); }
    public static double sqrt(double x) { return Math.sqrt(x); }
    public static double cbrt(double x) { return Math.cbrt(x); }
    public static double pow(double base, double exp) { return Math.pow(base, exp); }
    public static double log(double x) { return Math.log(x); }
    public static double log2(double x) { return Math.log(x) / Math.log(2); }
    public static double log10(double x) { return Math.log10(x); }
    public static double sin(double x) { return Math.sin(x); }
    public static double cos(double x) { return Math.cos(x); }
    public static double tan(double x) { return Math.tan(x); }
    public static double asin(double x) { return Math.asin(x); }
    public static double acos(double x) { return Math.acos(x); }
    public static double atan(double x) { return Math.atan(x); }
    public static double atan2(double y, double x) { return Math.atan2(y, x); }
    public static double toRadians(double deg) { return Math.toRadians(deg); }
    public static double toDegrees(double rad) { return Math.toDegrees(rad); }
    public static double min(double a, double b) { return Math.min(a, b); }
    public static double max(double a, double b) { return Math.max(a, b); }
    public static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }
    public static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    public static double signum(double x) { return Math.signum(x); }
    public static boolean isNaN(double x) { return Double.isNaN(x); }
    public static boolean isFinite(double x) { return Double.isFinite(x); }
    public static boolean isInfinite(double x) { return Double.isInfinite(x); }
    public static double random() { return Math.random(); }
}
