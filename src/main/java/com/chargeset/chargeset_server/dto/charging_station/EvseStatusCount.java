package com.chargeset.chargeset_server.dto.charging_station;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvseStatusCount {
    private String status;
    private int count;
}
