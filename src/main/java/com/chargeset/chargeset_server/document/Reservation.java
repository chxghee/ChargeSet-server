package com.chargeset.chargeset_server.document;

import com.chargeset.chargeset_server.document.status.ReservationStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reservation")
public class Reservation {
    @Id
    private String id;

    private String stationId;
    private String evseId;
    private int connectorId;

    private String userId;
    private String idToken;

    private Instant startTime;
    private Instant endTime;
    private int targetEnergyWh;

    private ReservationStatus reservationStatus;        // ACTIVE, WAITING, ONGOING, EXPIRED, COMPLETED, CANCELED
    private Instant createdAt;

}
