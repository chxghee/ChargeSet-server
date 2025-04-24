package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.document.Reservation;
import com.chargeset.chargeset_server.document.status.ReservationStatus;
import com.chargeset.chargeset_server.dto.reservation.NoShowCountResponse;
import com.chargeset.chargeset_server.dto.reservation.ReservationInfoResponse;
import com.chargeset.chargeset_server.dto.reservation.ReservationNoShowCount;
import com.chargeset.chargeset_server.repository.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    /**
     * 1. 금일 예약 현황 조회
     */
    public Page<ReservationInfoResponse> getReservationDailyStats(Pageable pageable) {
        return reservationRepository.findTodayReservations(pageable);
    }

    /**
     * 2. 전체 예약 조회 (검색 필터)
     */
    public Page<ReservationInfoResponse> getReservationStats(LocalDate from, LocalDate to, String stationId,
                                                             ReservationStatus reservationStatus, Pageable pageable) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일은 조회 종료일 보다 앞서야 합니다");
        }
        return reservationRepository.findReservationsWithFilter(from, to, stationId, reservationStatus, pageable);
    }

    /**
     * 3. 충전소별 No Show / complete 예약 집계
     */
    public NoShowCountResponse getNoShowCount() {
        List<ReservationNoShowCount> results = reservationRepository.getNoShowCounts();

        long totalNoShowCount = 0;
        long totalCompleteCount = 0;
        int totalStationCount = 0;
        for (ReservationNoShowCount result : results) {
            totalNoShowCount += result.getExpiredCount();
            totalCompleteCount += result.getCompleteCount();
            totalStationCount++;
        }

        return new NoShowCountResponse(totalStationCount, totalNoShowCount, totalCompleteCount, results);
    }
}
