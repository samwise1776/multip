package standardlibrary.dates;

import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

/**
 * Multip Standard Library — Dates Module
 * Provides date and time functions.
 */
public class MultipDates {
    public static long now() { return System.currentTimeMillis(); }
    public static long timestamp() { return Instant.now().getEpochSecond(); }
    public static String iso() { return Instant.now().toString(); }
    public static String format(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }
    public static String format(long millis, String pattern) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
    }
    public static int year() { return LocalDate.now().getYear(); }
    public static int month() { return LocalDate.now().getMonthValue(); }
    public static int day() { return LocalDate.now().getDayOfMonth(); }
    public static int hour() { return LocalTime.now().getHour(); }
    public static int minute() { return LocalTime.now().getMinute(); }
    public static int second() { return LocalTime.now().getSecond(); }
    public static String dayOfWeek() { return LocalDate.now().getDayOfWeek().toString(); }
    public static boolean isLeapYear(int year) { return Year.isLeap(year); }
    public static int daysInMonth(int year, int month) { return YearMonth.of(year, month).lengthOfMonth(); }
    public static long diff(String date1, String date2) {
        LocalDate d1 = LocalDate.parse(date1);
        LocalDate d2 = LocalDate.parse(date2);
        return ChronoUnit.DAYS.between(d1, d2);
    }
    public static String addDays(String date, int days) {
        return LocalDate.parse(date).plusDays(days).toString();
    }
    public static String addMonths(String date, int months) {
        return LocalDate.parse(date).plusMonths(months).toString();
    }
    public static String today() { return LocalDate.now().toString(); }
    public static String time() { return LocalTime.now().toString(); }
    public static String dateTime() { return LocalDateTime.now().toString(); }
    public static long parse(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    public static String fromTimestamp(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toString();
    }
}
