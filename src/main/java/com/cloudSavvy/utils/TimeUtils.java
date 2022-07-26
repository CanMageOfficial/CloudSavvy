package com.cloudSavvy.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
public class TimeUtils {
    private static final Format FILE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final Format USER_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static final Format DAY_FORMATTER = new SimpleDateFormat("MM/dd/yyyy");

    public static final SimpleDateFormat ISO_8601_SIMPLE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static long getElapsedTimeInDays(Instant startTime) {
        if (startTime == null) {
            return 0;
        }

        return calcDiffInDays(startTime, Instant.now());
    }

    public static long getElapsedTimeInDays(long timestamp) {
        Date lastEventDate = new Date(timestamp);

        return getElapsedTimeInDays(lastEventDate.toInstant());
    }

    private static long calcDiffInDays(Instant startTime, Instant endTime) {
        Duration diff = Duration.between(startTime, endTime);
        return diff.toDays();
    }

    public static long calcDiffInMillis(Instant startTime, Instant endTime) {
        Duration diff = Duration.between(startTime, endTime);
        return diff.toMillis();
    }

    public static String getDiffInPrettyFormat(Instant startTime, Instant endTime) {
        Duration diff = Duration.between(startTime, endTime);

        StringBuilder sb = new StringBuilder();
        if (diff.toMinutesPart() != 0) {
            sb.append(diff.toMinutesPart()).append(" minutes, ");
        }

        if (diff.toSecondsPart() != 0) {
            sb.append(diff.toSecondsPart()).append(" seconds");
        }

        return sb.toString();
    }

    public static String getFileFormattedCurrentTime() {
        return FILE_FORMATTER.format(new Date());
    }

    public static String getUserFormattedCurrentTime() {
        return USER_FORMATTER.format(new Date());
    }

    public static String getUserFormattedTime(Instant instant) {
        return USER_FORMATTER.format(Date.from(instant));
    }

    public static String getReadableDayFormat() {
        return DAY_FORMATTER.format(new Date());
    }

    public static Instant convertToInstant(String text) {
        try {
            return ISO_8601_SIMPLE_FORMAT.parse(text).toInstant();
        } catch (Exception exc) {
            log.error("Parsing {} failed", text);
            return null;
        }
    }
}
