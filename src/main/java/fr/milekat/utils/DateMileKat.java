package fr.milekat.utils;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple personal lib to format my dates
 */
@SuppressWarnings("unused")
public class DateMileKat {
    private static final Pattern periodPattern = Pattern.compile("([0-9]+)([smhjd])");
    private static final DateFormat customDf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final DateFormat systemDf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    @SuppressWarnings("SpellCheckingInspection")
    private static final DateFormat elasticDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Parses a date string in the custom format.
     *
     * @param date The date string to parse.
     * @return The parsed Date object.
     * @throws ParseException If an error occurs during parsing.
     */
    public static Date getCtmStringDate(String date) throws ParseException {
        return customDf.parse(date);
    }

    /**
     * Parses a date string in the system format.
     *
     * @param date The date string to parse.
     * @return The parsed Date object.
     * @throws ParseException If an error occurs during parsing.
     */
    public static Date getSysStringDate(String date) throws ParseException {
        return systemDf.parse(date);
    }

    /**
     * Parses a date string in the Elasticsearch format.
     *
     * @param date The date string to parse.
     * @return The parsed Date object.
     * @throws ParseException If an error occurs during parsing.
     */
    public static Date getESStringDate(String date) throws ParseException {
        return elasticDf.parse(date);
    }

    /**
     * Formats a Date object to a custom date string.
     *
     * @param date The Date object to format.
     * @return The formatted date string.
     */
    public static @NotNull String getDateCtm(Date date) {
        return customDf.format(date);
    }

    /**
     * Formats a Date object to a system date string.
     *
     * @param date The Date object to format.
     * @return The formatted date string.
     */
    public static @NotNull String getDateSys(Date date) {
        return systemDf.format(date);
    }

    /**
     * Formats a Date object to an Elasticsearch date string.
     *
     * @param date The Date object to format.
     * @return The formatted date string.
     */
    public static @NotNull String getDateEs(Date date) {
        return elasticDf.format(date);
    }

    /**
     * Formats the current date to a custom date string.
     *
     * @return The formatted date string.
     */
    public static @NotNull String getDateCtm() {
        return getDateCtm(new Date());
    }

    /**
     * Formats the current date to a system date string.
     *
     * @return The formatted date string.
     */
    public static @NotNull String getDateSys() {
        return getDateSys(new Date());
    }

    /**
     * Formats the current date to an Elasticsearch date string.
     *
     * @return The formatted date string.
     */
    public static @NotNull String getDateEs() {
        return getDateEs(new Date());
    }

    /**
     * Converts the remaining time until a specified date to a formatted string.
     *
     * @param date The target date.
     * @return The formatted string representing the remaining time.
     */
    public static String reamingToString(Date date) {
        HashMap<String, String> reamingTime = DateMileKat.getReamingTime(date, new Date());
        String time = "";
        if (!reamingTime.get("D").equals("0")) {
            time = reamingTime.get("D") + "jours ";
        }
        if (!reamingTime.get("h").equals("0")) {
            time = time + reamingTime.get("h") + "h ";
        }
        if (!reamingTime.get("m").equals("0")) {
            time = time + reamingTime.get("m") + "m ";
        }
        if (!reamingTime.get("s").equals("0")) {
            time = time + reamingTime.get("s") + "s ";
        }
        return Tools.remLastChar(time);
    }

    /**
     * Compare time between 2 dates (date1 need to be lower than date2 if you want a positive value)
     *
     * @param date1 The first date.
     * @param date2 The second date.
     * @return A HashMap with keys "D" (days), "h" (hours), "m" (minutes), "s" (seconds), and "ms" (milliseconds).
     */
    public static @NotNull HashMap<String, String> getReamingTime(@NotNull Date date1, @NotNull Date date2) {
        HashMap<String, String> RtHashMap = new HashMap<>();
        long diff = date1.getTime() - date2.getTime();
        RtHashMap.put("ms", String.valueOf(diff));
        RtHashMap.put("s", String.valueOf(diff / 1000 % 60));
        RtHashMap.put("m", String.valueOf(diff / (60 * 1000) % 60));
        RtHashMap.put("h", String.valueOf(diff / (60 * 60 * 1000) % 24));
        RtHashMap.put("D", String.valueOf(diff / (24 * 60 * 60 * 1000)));
        return RtHashMap;
    }

    /**
     * Parses a period string and converts it to a duration in milliseconds.
     *
     * @param period The period string to parse.
     * @return The duration in milliseconds.
     */
    public static Long parsePeriod(String period) {
        if (period == null) return null;
        period = period.toLowerCase(Locale.ROOT);
        Matcher matcher = periodPattern.matcher(period);
        Instant instant = Instant.EPOCH;
        while (matcher.find()) {
            int num = Integer.parseInt(matcher.group(1));
            switch (matcher.group(2)) {
                case "d":
                case "j": {
                    instant = instant.plus(Duration.ofDays(num));
                    break;
                }
                case "h": {
                    instant = instant.plus(Duration.ofHours(num));
                    break;
                }
                case "m": {
                    instant = instant.plus(Duration.ofMinutes(num));
                    break;
                }
                case "s": {
                    instant = instant.plus(Duration.ofSeconds(num));
                    break;
                }
            }
        }
        return instant.toEpochMilli();
    }
}