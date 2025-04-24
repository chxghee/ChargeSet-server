package com.chargeset.chargeset_server.dto.reservation;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoShowCountResponse {
    private int totalStationCount;
    private double totalNoShowRate;
    private long totalNoShowCount;
    private long totalCompleteCount;
    private long totalReservationCount;
    private List<ReservationNoShowCount> data;

    public NoShowCountResponse(int totalStationCount, long totalNoShowCount, long totalCompleteCount, List<ReservationNoShowCount> data) {
        this.totalStationCount = totalStationCount;
        this.totalNoShowCount = totalNoShowCount;
        this.totalCompleteCount = totalCompleteCount;
        this.totalReservationCount = totalCompleteCount + totalNoShowCount;
        this.totalNoShowRate = (double) totalNoShowCount / this.totalReservationCount;
        this.data = data;
    }
}
