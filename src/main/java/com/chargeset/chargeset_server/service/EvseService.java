package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.dto.charging_station.EvseStatusCount;
import com.chargeset.chargeset_server.dto.charging_station.EvseStatusSummary;
import com.chargeset.chargeset_server.repository.evse.EvseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EvseService {

    private final EvseRepository evseRepository;

    /**
     * 1. 모든 충전소의 사용 가능한 충전기 통계
     */
    public EvseStatusSummary getEvseStatusSummary() {
        List<EvseStatusCount> evseStatusCounts = evseRepository.countEvseStatus();
        return new EvseStatusSummary(evseStatusCounts);
    }

    /**
     * 2. 개별 충전소의 사용 가능한 충전기 통계
     */
    public EvseStatusSummary getEvseStatusSummaryPerStation(String stationId) {
        List<EvseStatusCount> evseStatusCounts = evseRepository.countEvseStatusByStationId(stationId);
        return new EvseStatusSummary(evseStatusCounts);
    }

}
