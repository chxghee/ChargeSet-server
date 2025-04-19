package com.chargeset.chargeset_server.utils;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class TimeUtils {

    public static Pair<Instant, Instant> getTodayRangeInKST() {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(KST);
        Instant startOfDay = today.atStartOfDay(KST).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(KST).toInstant();
        return Pair.of(startOfDay, endOfDay);
    }

    public static Pair<Instant, Instant> getWeeklyRangeInKST() {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(KST);
        LocalDate sevenDaysAgo = today.minusDays(6);
        Instant startOfDay = sevenDaysAgo.atStartOfDay(KST).toInstant();
        Instant endOfDay = today.plusDays(1).atStartOfDay(KST).toInstant();
        return Pair.of(startOfDay, endOfDay);
    }

    public static Pair<Instant, Instant> getInputDayRangeInKST(LocalDate date) {
        ZoneId KST = ZoneId.of("Asia/Seoul");
        Instant startOfDay = date.atStartOfDay(KST).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(KST).toInstant();
        return Pair.of(startOfDay, endOfDay);
    }

    public static Instant convertDateToUTC(LocalDate date) {
        return date.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
    }

}
