package fr.milekat.utils;

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
public class DateMileKat {
    private static final Pattern periodPattern = Pattern.compile("([0-9]+)([smhjd])");
    private static final DateFormat customDf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final DateFormat systemDf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final DateFormat elasticDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static Date getCtmStringDate(String date) throws ParseException {
        return customDf.parse(date);
    }

    public static Date getSysStringDate(String date) throws ParseException {
        return systemDf.parse(date);
    }

    public static Date getESStringDate(String date) throws ParseException {
        return elasticDf.parse(date);
    }

    public static String getDateCtm(Date date) {
        return customDf.format(date);
    }

    public static String getDateSys(Date date) {
        return systemDf.format(date);
    }

    public static String getDateEs(Date date) {
        return elasticDf.format(date);
    }

    public static String getDateCtm() {
        return getDateCtm(new Date());
    }

    public static String getDateSys() {
        return getDateSys(new Date());
    }

    public static String getDateEs() {
        return getDateEs(new Date());
    }

    /**
     *      Send reaming time until Date !
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
     * @return HashMap with D h m s ms
     */
    public static HashMap<String, String> getReamingTime(Date date1, Date date2) {
        HashMap<String, String> RtHashMap = new HashMap<>();
        long diff = date1.getTime() - date2.getTime();
        RtHashMap.put("ms", "" + diff);
        RtHashMap.put("s", "" + diff / 1000 % 60);
        RtHashMap.put("m", "" + diff / (60 * 1000) % 60);
        RtHashMap.put("h", "" + diff / (60 * 60 * 1000) % 24);
        RtHashMap.put("D", "" + diff / (24 * 60 * 60 * 1000));
        return RtHashMap;
    }

    /**
     * Convert 4j5h9m3s in duration
     * @param period String to transform
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