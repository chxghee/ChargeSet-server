package com.chargeset.chargeset_server.utils;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;

@Component
public class TimeUtils {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Pair<Instant, Instant> getTodayRangeInKST() {
        return getRangeInKST(LocalDate.now(KST), LocalDate.now(KST));
    }

    public static Pair<Instant, Instant> getWeeklyRangeInKST() {
        LocalDate today = LocalDate.now(KST);
        return getRangeInKST(today.minusDays(6), today);
    }

    public static Pair<Instant, Instant> getMonthlyRangeInKST() {
        LocalDate today = LocalDate.now(KST);
        return getRangeInKST(today.minusDays(30), today);
    }

    public static Pair<LocalDate, LocalDate> getMonthlyRangeByMonth(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return Pair.of(from, to);
    }

    public static Pair<Instant, Instant> getUTCRangeInKST(LocalDate from, LocalDate to) {
        return getRangeInKST(from, to);
    }

    public static Pair<Instant, Instant> getInputDayRangeInKST(LocalDate date) {
        return getRangeInKST(date, date);
    }

    public static Instant convertDateToUTC(LocalDate date) {
        return date.atStartOfDay(KST).toInstant();
    }

    public static Instant convertDateTimeToUTC(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    public static LocalDateTime convertInstantToKST(Instant instant) {
        return LocalDateTime.ofInstant(instant, KST);
    }

    public static String formatInstantToKSTString(Instant instant) {
        return convertInstantToKST(instant).format(DEFAULT_FORMATTER);
    }

    public static Pair<Instant, Instant> getUTCTimeRangeInKST(LocalDateTime from, LocalDateTime to) {
        Instant start = from.atZone(KST).toInstant();
        Instant end = to.atZone(KST).toInstant();
        return Pair.of(start, end);
    }

    private static Pair<Instant, Instant> getRangeInKST(LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(KST).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(KST).toInstant(); // inclusive
        return Pair.of(start, end);
    }
}
