package com.chargeset.chargeset_server.service;

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
import java.time.ZoneId;
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
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 현재 월 범위
        LocalDate currentFrom = today.minusDays(29);
        LocalDate currentTo = today;
        List<ReservationNoShowCount> currentStats = reservationRepository.getNoShowCounts(currentFrom, currentTo);

        for (ReservationNoShowCount currentStat : currentStats) {
            System.out.println("노쇼 " + currentStat.getCompleteCount() + " " + currentStat.getStationId() + " " + currentStat.getExpiredCount());
        }

        // 누적값 계산
        long totalNoShowCount = currentStats.stream().mapToLong(ReservationNoShowCount::getExpiredCount).sum();
        long totalCompleteCount = currentStats.stream().mapToLong(ReservationNoShowCount::getCompleteCount).sum();
        int totalStationCount = currentStats.size();
        long currentTotal = totalNoShowCount + totalCompleteCount;
        double totalNoShowRate = currentTotal == 0 ? 0.0 : (double) totalNoShowCount / currentTotal;



        // 지난달 범위
        LocalDate previousFrom = today.minusDays(59);
        LocalDate previousTo = today.minusDays(30);
        List<ReservationNoShowCount> previousStats = reservationRepository.getNoShowCounts(previousFrom, previousTo);


        long previousNoShowCount = previousStats.stream().mapToLong(ReservationNoShowCount::getExpiredCount).sum();
        long previousCompleteCount = previousStats.stream().mapToLong(ReservationNoShowCount::getCompleteCount).sum();
        long previousTotal = previousNoShowCount + previousCompleteCount;

        // 지난달 노쇼율 계산
        double previousNoShowRate = previousTotal == 0 ? 0.0 : (double) previousNoShowCount / previousTotal;

        return new NoShowCountResponse(
                totalStationCount,
                totalNoShowRate,
                totalNoShowCount,
                totalCompleteCount,
                currentTotal,
                currentStats,
                currentFrom,
                currentTo,
                previousNoShowRate
        );
    }
}
