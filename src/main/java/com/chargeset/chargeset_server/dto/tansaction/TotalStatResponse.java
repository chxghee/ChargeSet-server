package com.chargeset.chargeset_server.dto.tansaction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TotalStatResponse {

    private long stationCount;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<StationStatReport> stationChargingStats;;
    private long totalCount;
    private long totalEnergy;
    private long totalRevenue;
    private long totalRevenueLastMonth;
    private double revenueGrowthRate;

    public TotalStatResponse(long stationCount, LocalDate fromDate, LocalDate toDate, List<StationStatReport> stationChargingStats,
                              long totalCount, long totalEnergy, long totalRevenue, long totalRevenueLastMonth) {
        this.stationCount = stationCount;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.stationChargingStats = stationChargingStats;
        this.totalCount = totalCount;
        this.totalEnergy = totalEnergy;
        this.totalRevenue = totalRevenue;
        this.totalRevenueLastMonth = totalRevenueLastMonth;
        if (totalRevenueLastMonth == 0) {
            this.revenueGrowthRate = (totalRevenue == 0) ? 0.0 : 100.0;
        } else {
            this.revenueGrowthRate = ((double) (totalRevenue - totalRevenueLastMonth) / totalRevenueLastMonth) * 100;
        }
    }
}
