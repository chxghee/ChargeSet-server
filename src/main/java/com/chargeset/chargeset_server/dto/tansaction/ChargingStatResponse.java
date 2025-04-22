package com.chargeset.chargeset_server.dto.tansaction;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChargingStatResponse {
    private long totalCount;
    private long totalRevenue;
    private long totalEnergy;
    private List<ChargingDailyStat> dailyStats;
}
