package fr.milekat.utils;

import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class DateMileKatTest {
    @Test
    void testGetCtmStringDate() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        Date expectedDate = calendar.getTime();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateString = df.format(expectedDate);
        Date actualDate = DateMileKat.getCtmStringDate(dateString);
        assertEquals(expectedDate.getTime(), actualDate.getTime());
    }

    @Test
    void testGetSysStringDate() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        Date expectedDate = calendar.getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dateString = df.format(expectedDate);
        Date actualDate = DateMileKat.getSysStringDate(dateString);
        assertEquals(expectedDate.getTime(), actualDate.getTime());
    }

    @Test
    void testGetESStringDate() throws ParseException {
        Date expectedDate = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateString = df.format(expectedDate);
        Date actualDate = DateMileKat.getESStringDate(dateString);
        assertEquals(expectedDate.getTime(), actualDate.getTime());
    }

    @Test
    void testGetDateCtm() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String expectedDateString = df.format(date);
        String actualDateString = DateMileKat.getDateCtm(date);
        assertEquals(expectedDateString, actualDateString);
    }

    @Test
    void testGetDateSys() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String expectedDateString = df.format(date);
        String actualDateString = DateMileKat.getDateSys(date);
        assertEquals(expectedDateString, actualDateString);
    }

    @Test
    void testGetDateEs() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String expectedDateString = df.format(date);
        String actualDateString = DateMileKat.getDateEs(date);
        assertEquals(expectedDateString, actualDateString);
    }

    @Test
    void testGetDateCtmNow() {
        assertNotNull(DateMileKat.getDateCtm());
    }

    @Test
    void testGetDateSysNow() {
        assertNotNull(DateMileKat.getDateSys());
    }

    @Test
    void testGetDateEsNow() {
        assertNotNull(DateMileKat.getDateEs());
    }

    @Test
    void testReamingToString() {
        assertNotNull(DateMileKat.reamingToString(new Date()));
    }


    @Test
    void testParsePeriod() {
        assertEquals(86400000L, DateMileKat.parsePeriod("1d"));
        assertEquals(90000000L, DateMileKat.parsePeriod("1d1h"));
        assertEquals(90180000L, DateMileKat.parsePeriod("1d1h3m"));
        assertEquals(90183000L, DateMileKat.parsePeriod("1d1h3m3s"));
        assertEquals(9000000L, DateMileKat.parsePeriod("2h30m"));
        assertEquals(180000L, DateMileKat.parsePeriod("3m"));
        assertEquals(1000L, DateMileKat.parsePeriod("1s"));
        assertEquals(0L, DateMileKat.parsePeriod("0s"));
        assertEquals(0L, DateMileKat.parsePeriod(""));
        assertNull(DateMileKat.parsePeriod(null));
    }

}