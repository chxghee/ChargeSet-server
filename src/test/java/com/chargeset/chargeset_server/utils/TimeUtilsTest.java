package com.chargeset.chargeset_server.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;

class TimeUtilsTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Test
    void getTodayRangeInKST_정확한_오늘범위반환() {
        LocalDate today = LocalDate.now(KST);
        Pair<Instant, Instant> range = TimeUtils.getTodayRangeInKST();

        assertThat(range.getFirst()).isEqualTo(today.atStartOfDay(KST).toInstant());
        assertThat(range.getSecond()).isEqualTo(today.plusDays(1).atStartOfDay(KST).toInstant());
    }

    @Test
    void convertInstantToKST_정확한_KST_시간변환() {
        LocalDateTime expected = LocalDateTime.of(2024, 4, 1, 0, 0);
        Instant instant = expected.atZone(KST).toInstant();

        LocalDateTime converted = TimeUtils.convertInstantToKST(instant);
        assertThat(converted).isEqualTo(expected);
    }

    @Test
    void formatInstantToKSTString_형식맞게_변환된다() {
        LocalDateTime datetime = LocalDateTime.of(2024, 4, 1, 15, 30);
        Instant instant = datetime.atZone(KST).toInstant();

        String formatted = TimeUtils.formatInstantToKSTString(instant);
        assertThat(formatted).isEqualTo("2024-04-01 15:30:00");
    }

}
