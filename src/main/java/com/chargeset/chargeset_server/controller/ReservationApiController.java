package com.chargeset.chargeset_server.controller;

import com.chargeset.chargeset_server.document.status.ReservationStatus;
import com.chargeset.chargeset_server.dto.reservation.NoShowCountResponse;
import com.chargeset.chargeset_server.dto.reservation.ReservationInfoResponse;
import com.chargeset.chargeset_server.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@Slf4j
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;

    /**
     * 1. 금일 예약 조회 - 대시보드
     */
    @GetMapping("/today-stat")
    public Page<ReservationInfoResponse> todayStat(@PageableDefault(size = 5) Pageable pageable) {
        return reservationService.getReservationDailyStats(pageable);
    }

    /**
     * 2. 전체 예약 조회 (검색 필터) - 예약
     */
    @GetMapping("/all")
    public Page<ReservationInfoResponse> all(@PageableDefault(size = 10) Pageable pageable,
                                             @RequestParam(name = "from", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam(name = "to", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                             @RequestParam(name = "stationId", required = false) String stationId,
                                             @RequestParam(name = "status", required = false) ReservationStatus status) {

        // 기본 3개월 검색
        if (to == null) to = LocalDate.now();
        if (from == null) from = LocalDate.now().minusMonths(3);
        return reservationService.getReservationStats(from, to, stationId, status, pageable);
    }

    /**
     * 3. 노쇼율 집계 - 예약
     */
    @GetMapping("/no-show")
    public ResponseEntity<NoShowCountResponse> noShow() {
        return ResponseEntity.ok(reservationService.getNoShowCount());
    }

}
