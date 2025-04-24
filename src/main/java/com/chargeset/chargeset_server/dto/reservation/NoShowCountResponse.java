package com.chargeset.chargeset_server.dto.reservation;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoShowCountResponse {
    private int totalStationCount;
    private double totalNoShowRate;
    private long totalNoShowCount;
    private long totalCompleteCount;
    private long totalFinishedReservationCount;
    private List<ReservationNoShowCount> data;
    private LocalDate fromDate;
    private LocalDate toDate;
    private double previousNoShowRate;

}
