package com.chargeset.chargeset_server.document;

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

}
