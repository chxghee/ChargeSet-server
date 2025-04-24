package com.chargeset.chargeset_server.dto.tansaction;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HourlyStatResponse {
    private LocalDate date;
    private String stationId;
    private long dailyTotalCount;
    private long dailyTotalRevenue;
    private long dailyTotalEnergy;
    private List<ChargingHourlyStat> data;
}
