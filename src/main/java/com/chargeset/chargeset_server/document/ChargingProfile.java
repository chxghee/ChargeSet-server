package com.chargeset.chargeset_server.document;

import com.chargeset.chargeset_server.dto.reservation.NewChargingProfile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "chargingProfile")
public class ChargingProfile {

    @Id
    private String id;

    private String reservationId;
    private String chargingProfileKind;     // ABSOLUTE, RECURRING, RELATIVE  (default = ABSOLUTE)
    private Instant startSchedule;
    private List<ChargingSchedulePeriod> chargingSchedules;

    public void submit(String reservationId, NewChargingProfile newChargingProfile) {
        this.reservationId = reservationId;
        this.chargingProfileKind = newChargingProfile.getChargingProfileKind();
        this.startSchedule = newChargingProfile.getStartSchedule();
        this.chargingSchedules = newChargingProfile.getChargingSchedules();
    }

}
