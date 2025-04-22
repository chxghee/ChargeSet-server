package com.chargeset.chargeset_server.utils;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class TimeUtils {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Pair<Instant, Instant> getTodayRangeInKST() {
        LocalDate today = LocalDate.now(KST);
        return getInputDayRangeInKST(today);
    }

    public static Pair<Instant, Instant> getWeeklyRangeInKST() {
        LocalDate today = LocalDate.now(KST);
        LocalDate sevenDaysAgo = today.minusDays(6);
        Instant startOfDay = sevenDaysAgo.atStartOfDay(KST).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(KST).toInstant();
        return Pair.of(startOfDay, endOfDay);
    }

    public static Pair<Instant, Instant> getMonthlyRangeInKST() {
        LocalDate today = LocalDate.now(KST);
        LocalDate sevenDaysAgo = today.minusDays(30);
        Instant startOfDay = sevenDaysAgo.atStartOfDay(KST).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(KST).toInstant();
        return Pair.of(startOfDay, endOfDay);
    }

    public static Pair<Instant, Instant> getInputDayRangeInKST(LocalDate date) {
        Instant startOfDay = date.atStartOfDay(KST).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(KST).toInstant();
        return Pair.of(startOfDay, endOfDay);
    }

    public static Instant convertDateToUTC(LocalDate date) {
        return date.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
    }

    public static LocalDateTime convertInstantToKST(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }

    public static String formatInstantToKSTString(Instant instant) {
        return convertInstantToKST(instant).format(DEFAULT_FORMATTER);
    }
}
