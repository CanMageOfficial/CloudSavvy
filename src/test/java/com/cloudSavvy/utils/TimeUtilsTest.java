package com.cloudSavvy.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TimeUtilsTest {
    @Test
    public void test_getElapsedTimeInDays() {
        Instant startTime = Instant.now().minus(5, ChronoUnit.DAYS);
        assertEquals(5, TimeUtils.getElapsedTimeInDays(startTime));

        assertEquals(5, TimeUtils.getElapsedTimeInDays(startTime.toEpochMilli()));
    }

    @Test
    public void test_getFormattedCurrentTime() {
        String text = TimeUtils.getFileFormattedCurrentTime();
        assertNotNull(text, text);
    }

    @Test
    public void test_getReadableDayFormat() {
        String formattedDay = TimeUtils.getReadableDayFormat();
        assertNotNull(formattedDay, formattedDay);
    }

    @Test
    public void test_convertToInstant() {
        Instant instant = TimeUtils.convertToInstant("2022-12-01T08:46:19.000+0000");
        assertNotNull(instant);
    }
}
