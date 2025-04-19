package com.chargeset.chargeset_server.dto.reservation;

import lombok.Data;

@Data
public class ReservationInfoResponse {
    private String startTime;
    private String endTime;
    private String createdAt;
    private String id;
    private String stationId;
    private String evseId;
    private String userId;
    private int targetEnergyWh;
    private String reservationStatus;
}
