package com.chargeset.chargeset_server.dto.reservation;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationNoShowCount {
    private String stationId;
    private long completeCount;
    private long expiredCount;
}
