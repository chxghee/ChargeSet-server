package com.chargeset.chargeset_server.dto.tansaction;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyStatResponse {
    private long weeklyTotalCount;
    private long weeklyTotalRevenue;
    private long weeklyTotalEnergy;
    private List<ChargingDailyStat> data;
}
