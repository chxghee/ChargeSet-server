package com.chargeset.chargeset_server.dto.charging_station;

import com.chargeset.chargeset_server.document.Location;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingStationInfo {      // 위치 정보만 담는다

    private String id;
    private String stationId;             // 내부 시스템 식별자
    private String name;
    private Location location;
}
