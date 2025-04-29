package com.chargeset.chargeset_server.dto.tansaction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingStat {
    private String stationId;
    private long totalCount;
    private long totalRevenue;
    private long totalEnergy;
}
