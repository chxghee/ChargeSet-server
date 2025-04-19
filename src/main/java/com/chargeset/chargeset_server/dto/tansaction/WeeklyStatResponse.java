package com.chargeset.chargeset_server.dto.tansaction;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WeeklyStatResponse {
    private long weeklyTotalCount;
    private long weeklyTotalRevenue;
    private long weeklyTotalEnergy;
    private List<ChargingDailyStat> data;
}
