package com.chargeset.chargeset_server.dto.tansaction;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StationStatReport {
    private String stationId;
    private long totalCount;
    private long totalRevenue;
    private long totalEnergy;
    private long totalRevenueLastMonth;
    private double revenueGrowthRate;


    public StationStatReport(String stationId, long totalCount, long totalRevenue, long totalEnergy, long totalRevenueLastMonth) {
        this.stationId = stationId;
        this.totalCount = totalCount;
        this.totalRevenue = totalRevenue;
        this.totalEnergy = totalEnergy;
        this.totalRevenueLastMonth = totalRevenueLastMonth;
        if (totalRevenueLastMonth == 0) {
            this.revenueGrowthRate = (totalRevenue == 0) ? 0.0 : 100.0;
        } else {
            this.revenueGrowthRate = ((double) (totalRevenue - totalRevenueLastMonth) / totalRevenueLastMonth) * 100;
        }
    }
}
