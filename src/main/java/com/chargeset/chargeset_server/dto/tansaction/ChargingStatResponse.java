package com.chargeset.chargeset_server.dto.tansaction;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingStatResponse {
    private long totalCount;
    private long totalRevenue;
    private long totalEnergy;
    private List<ChargingDailyStat> dailyStats;
}
