package com.chargeset.chargeset_server.repository.evse;

import com.chargeset.chargeset_server.dto.charging_station.EvseStatusCount;

import java.util.List;

public interface EvseCustomRepository {

    // 1. 모든 충전소의 EVSE 현황
    List<EvseStatusCount> countEvseStatus();

    // 2. 개별 충전소의 EVSE 현황
    List<EvseStatusCount> countEvseStatusByStationId(String stationId);
}
