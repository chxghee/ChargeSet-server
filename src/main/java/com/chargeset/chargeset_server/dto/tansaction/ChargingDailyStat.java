package com.chargeset.chargeset_server.dto.tansaction;

import lombok.*;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingDailyStat {
    private LocalDate date;
    private long totalRevenue;  // 오늘 매출 원
    private long totalEnergy;   // 오늘 사용 전력 Wh
    private long count;         // 오늘 총 충전 횟수
}
