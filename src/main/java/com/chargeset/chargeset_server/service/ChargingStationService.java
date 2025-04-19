package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.dto.charging_station.ChargingStationInfo;
import com.chargeset.chargeset_server.repository.charging_station.ChargingStationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChargingStationService {

    private final ChargingStationRepository chargingStationRepository;

    /**
     * 1. 모든 충전소의 위치 가져오기
     */
    public List<ChargingStationInfo> getChargingStationsInfo() {
        return chargingStationRepository.findAllWithLocation();
    }

    /**
     * 2. 개별 충전소희 위치 가져오기
     */
    public ChargingStationInfo getChargingStationInfo(String stationId) {
        return chargingStationRepository.findByStationId(stationId);
    }


}
