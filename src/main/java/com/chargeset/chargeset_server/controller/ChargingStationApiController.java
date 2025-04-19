package com.chargeset.chargeset_server.controller;

import com.chargeset.chargeset_server.dto.charging_station.ChargingStationInfo;
import com.chargeset.chargeset_server.dto.tansaction.ChargingDailyStat;
import com.chargeset.chargeset_server.dto.tansaction.HourlyStatResponse;
import com.chargeset.chargeset_server.service.ChargingStationService;
import com.chargeset.chargeset_server.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class ChargingStationApiController {

    private final ChargingStationService chargingStationService;
    private final TransactionService transactionService;

    /**
     * 1. 모든 충전소의 정보 조회
     */
    @GetMapping("/all")
    public List<ChargingStationInfo> allStations() {
        return chargingStationService.getChargingStationsInfo();
    }

    /**
     * 2. 특정 충전소의 정보 조회
     */
    @GetMapping("/{stationId}")
    public ResponseEntity<ChargingStationInfo> getStation(@PathVariable String stationId) {
        return ResponseEntity.ok(chargingStationService.getChargingStationInfo(stationId));
    }

    /**
     * 3. 금일 충전소별 매출 현황 - 충전소별 현황
     */
    @GetMapping("/{stationId}/today-revenue")
    public ResponseEntity<ChargingDailyStat> getTodayRevenueStatByStation(@PathVariable("stationId") String stationId) {
        return ResponseEntity.ok(transactionService.getTodayChargingStatsByStationId(stationId));
    }

    /**
     * 4. 시간대별 운영 그래프 - 충전소별 현황
     */
    @GetMapping("/{stationId}/hourly-revenue")
    public ResponseEntity<HourlyStatResponse> getHourlyRevenueStatByStation(@PathVariable("stationId") String stationId,
                                                                            @RequestParam(name = "searchingDate", required = false)
                                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                            LocalDate searchingDate) {
        if (searchingDate == null) {
            searchingDate = LocalDate.now();
        }
        return ResponseEntity.ok(transactionService.getHourlyChargingStats(stationId, searchingDate));
    }
}
