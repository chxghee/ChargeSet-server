package com.chargeset.chargeset_server.dto.reservation;

import lombok.*;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NewReservation {

    private String stationId;
    private String evseId;
    private int connectorId = 1;

    private String userId;
    private String idToken;

    private Instant startTime;
    private Instant endTime;
    private int targetEnergyWh;
    private int cost;

    private NewChargingProfile chargingProfile;

}
