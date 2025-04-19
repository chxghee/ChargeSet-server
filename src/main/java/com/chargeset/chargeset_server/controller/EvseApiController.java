package com.chargeset.chargeset_server.controller;

import com.chargeset.chargeset_server.dto.charging_station.EvseStatusSummary;
import com.chargeset.chargeset_server.service.EvseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/evses")
@RequiredArgsConstructor
public class EvseApiController {

    private final EvseService evseService;

    /**
     * 1. 전체 충전소 EVSE 현황 통계
     */
    @GetMapping("/status-summary")
    public ResponseEntity<EvseStatusSummary> getEvseStatusSummary() {
        return ResponseEntity.ok(evseService.getEvseStatusSummary());
    }

    /**
     * 2. 개별 충전소 EVSE 현황 통계
     */
    @GetMapping("/{stationId}/status-summary")
    public ResponseEntity<EvseStatusSummary> getEvseStatusSummaryPerStation(@PathVariable(name = "stationId") String stationId) {
        return ResponseEntity.ok(evseService.getEvseStatusSummaryPerStation(stationId));
    }



}
