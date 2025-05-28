package com.chargeset.chargeset_server.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StationChargingCost {

    ST001("ST-001", 30, 360),
    ST002("ST-002", 25, 365);

    private final String stationId;
    private final int standardChargingCostPerMinute;
    private final int fastChargingCostPerMinute;

    public static StationChargingCost from(String stationId) {
        for (StationChargingCost chargingCost : values()) {
            if (chargingCost.stationId.equals(stationId)) {
                return chargingCost;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 충전소입니다. : " + stationId);
    }
}
