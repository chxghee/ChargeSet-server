package com.chargeset.chargeset_server.dto.tansaction;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class HourlyStatResponse {
    private LocalDate date;
    private String stationId;
    private long dailyTotalCount;
    private long dailyTotalRevenue;
    private long dailyTotalEnergy;
    private List<ChargingHourlyStat> data;
}
