package com.chargeset.chargeset_server.dto.tansaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingDailyStat {
    private LocalDate date;
    private long totalRevenue;  // 오늘 매출 원
    private long totalEnergy;   // 오늘 사용 전력 Wh
    private long count;         // 오늘 총 충전 횟수
}
