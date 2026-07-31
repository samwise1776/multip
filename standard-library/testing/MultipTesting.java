package standardlibrary.testing;

import java.util.*;

/**
 * Multip Standard Library — Testing Module
 * Provides test assertions and framework.
 */
public class MultipTesting {
    private static int passed = 0;
    private static int failed = 0;
    private static int total = 0;
    private static final List<String> failures = new ArrayList<>();

    public static void reset() { passed = 0; failed = 0; total = 0; failures.clear(); }

    public static void assertEqual(String name, Object expected, Object actual) {
        total++;
        if (Objects.equals(expected, actual)) {
            passed++;
            System.out.println("  ✓ " + name);
        } else {
            failed++;
            String msg = "  ✗ " + name + " — expected: " + expected + ", got: " + actual;
            failures.add(msg);
            System.out.println(msg);
        }
    }

    public static void assertNotEqual(String name, Object expected, Object actual) {
        total++;
        if (!Objects.equals(expected, actual)) {
            passed++;
            System.out.println("  ✓ " + name);
        } else {
            failed++;
            String msg = "  ✗ " + name + " — should not be: " + actual;
            failures.add(msg);
            System.out.println(msg);
        }
    }

    public static void assertTrue(String name, boolean condition) {
        assertEqual(name, true, condition);
    }

    public static void assertFalse(String name, boolean condition) {
        assertEqual(name, false, condition);
    }

    public static void assertNull(String name, Object value) {
        assertEqual(name, null, value);
    }

    public static void assertNotNull(String name, Object value) {
        total++;
        if (value != null) { passed++; System.out.println("  ✓ " + name); }
        else { failed++; failures.add("  ✗ " + name + " — expected non-null"); System.out.println("  ✗ " + name + " — expected non-null"); }
    }

    public static void assertThrows(String name, Runnable test) {
        total++;
        try {
            test.run();
            failures.add("  ✗ " + name + " — expected exception");
            System.out.println("  ✗ " + name + " — expected exception");
            failed++;
        } catch (Exception e) {
            passed++;
            System.out.println("  ✓ " + name + " — threw " + e.getClass().getSimpleName());
        }
    }

    public static void assertClose(String name, double expected, double actual, double delta) {
        total++;
        if (Math.abs(expected - actual) <= delta) {
            passed++;
            System.out.println("  ✓ " + name);
        } else {
            failed++;
            String msg = "  ✗ " + name + " — expected ~" + expected + ", got " + actual;
            failures.add(msg);
            System.out.println(msg);
        }
    }

    public static int getPassed() { return passed; }
    public static int getFailed() { return failed; }
    public static int getTotal() { return total; }
    public static boolean allPassed() { return failed == 0; }

    public static void summary() {
        System.out.println();
        System.out.println("═══ Test Results ═══");
        System.out.println("  Passed: " + passed + "/" + total);
        System.out.println("  Failed: " + failed + "/" + total);
        if (failures.isEmpty()) {
            System.out.println("  All tests passed!");
        } else {
            System.out.println("  Failures:");
            for (String f : failures) System.out.println(f);
        }
    }
}
