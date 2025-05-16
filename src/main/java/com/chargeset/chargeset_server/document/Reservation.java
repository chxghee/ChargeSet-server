package com.chargeset.chargeset_server.document;

import com.chargeset.chargeset_server.document.status.ReservationStatus;
import com.chargeset.chargeset_server.dto.reservation.NewReservation;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reservation")
@Getter
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
    private int cost;

    private ReservationStatus reservationStatus;        // ACTIVE, WAITING, ONGOING, EXPIRED, COMPLETED, CANCELED
    private Instant createdAt;


    public void submit(NewReservation newReservation) {
        this.stationId = newReservation.getStationId();
        this.evseId = newReservation.getEvseId();
        this.connectorId = newReservation.getConnectorId();
        this.userId = newReservation.getUserId();
        this.idToken = newReservation.getIdToken();
        this.startTime = newReservation.getStartTime();
        this.endTime = newReservation.getEndTime();
        this.targetEnergyWh = newReservation.getTargetEnergyWh();
        this.cost = newReservation.getCost();
        this.reservationStatus = ReservationStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public void cancel() {
        this.reservationStatus = ReservationStatus.CANCELED;
    }

}
