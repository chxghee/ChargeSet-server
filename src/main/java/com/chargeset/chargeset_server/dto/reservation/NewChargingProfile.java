package com.chargeset.chargeset_server.dto.reservation;

import com.chargeset.chargeset_server.document.ChargingSchedulePeriod;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@AllArgsConstructor
public class NewChargingProfile {
    private final String chargingProfileKind = "ABSOLUTE";
    private Instant startSchedule;
    private List<ChargingSchedulePeriod> chargingSchedules;
}
