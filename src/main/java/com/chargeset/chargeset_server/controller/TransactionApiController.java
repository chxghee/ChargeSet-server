package com.chargeset.chargeset_server.controller;

import com.chargeset.chargeset_server.document.status.TransactionStatus;
import com.chargeset.chargeset_server.dto.tansaction.*;
import com.chargeset.chargeset_server.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionService transactionService;

    /**
     * 1. 금일 충전소 매출 현황 - 대시보드
     */
    @GetMapping("/today-revenue")
    public ResponseEntity<ChargingDailyStat> getTodayRevenueStat() {
        return ResponseEntity.ok(transactionService.getTodayChargingStats());
    }

    /**
     * 2. 주간 운영 그래프 - 대시보드
     */
    @GetMapping("/weekly-revenue")
    public ResponseEntity<WeeklyStatResponse> getWeeklyRevenueStat() {
        return ResponseEntity.ok(transactionService.getWeeklyChargingStats());
    }

    /**
     * 3. 충전 이력 조회 - 충전 이력
     */
    @GetMapping("/all")
    public Page<TransactionInfoResponse> all(@PageableDefault(size = 10) Pageable pageable,
                                             @RequestParam(name = "from", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam(name = "to", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                             @RequestParam(name = "stationId", required = false) String StationId,
                                             @RequestParam(name = "status", required = false) TransactionStatus status) {
        // 기본 3개월 검색
        if (to == null) to = LocalDate.now();
        if (from == null) from = LocalDate.now().minusMonths(3);
        return transactionService.getChargingStats(from, to, StationId, status, pageable);
    }

    /**
     * 4. 충전 프로파일 조회
     */
    @GetMapping("/{transactionId}/charging-profile")
    public ResponseEntity<ChargingProfileResponse> getChargingProfile(@PathVariable(name = "transactionId") String transactionId) {
        return ResponseEntity.ok(transactionService.getChargingProfile(transactionId));
    }

}
