package com.chargeset.chargeset_server.repository.evse;

import com.chargeset.chargeset_server.dto.EvseIdOnly;
import com.chargeset.chargeset_server.dto.charging_station.EvseStatusCount;

import java.util.List;

public interface EvseCustomRepository {

    // 1. 모든 충전소의 EVSE 현황
    List<EvseStatusCount> countEvseStatus();

    // 2. 개별 충전소의 EVSE 현황
    List<EvseStatusCount> countEvseStatusByStationId(String stationId);

    // 3. 전단된 충전소에 포함되지 않는 충전소 찾기
    List<EvseIdOnly> findAvailableEvseIds(List<String> occupiedEvseIds, String stationId);
}
