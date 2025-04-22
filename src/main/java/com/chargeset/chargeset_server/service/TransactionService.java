package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.document.status.TransactionStatus;
import com.chargeset.chargeset_server.dto.tansaction.*;
import com.chargeset.chargeset_server.repository.charging_station.ChargingStationRepository;
import com.chargeset.chargeset_server.repository.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ChargingStationRepository chargingStationRepository;

    /**
     * 1. 전체 금일 매출, 전력, 사용횟수 조회
     */
    public ChargingDailyStat getTodayChargingStats() {
        return transactionRepository.getTodayChargingStats()
                .orElseGet(() -> new ChargingDailyStat(LocalDate.now(),0,0,0));
    }

    /**
     * 2. 충전소별 금일 매출, 전력, 사용횟수 조회
     */
    public ChargingDailyStat getTodayChargingStatsByStationId(String stationId) {
        return transactionRepository.getTodayChargingStatsByStationId(stationId)
                .orElseGet(() -> new ChargingDailyStat(LocalDate.now(),0,0,0));
    }


    /**
     * 3. 주간 매출, 전력, 사용횟수 조회
     */
    public WeeklyStatResponse getWeeklyChargingStats() {

        List<ChargingDailyStat> actualResults = transactionRepository.getWeeklyChargingStats();

        List<LocalDate> last7Days = getLastDaysList(6);

        return buildWeeklyStatResponse(actualResults, last7Days);
    }


    /**
     * 4. 시간대별 매출, 전력, 사용횟수 조회
     */
    public HourlyStatResponse getHourlyChargingStats(String stationId, LocalDate searchingDate) {
        List<ChargingHourlyStat> actualResults = transactionRepository.getHourlyChargingStats(stationId, searchingDate);

        return buildHourlyStatResponse(stationId, searchingDate, actualResults);
    }

    /**
     * 5. 전체 충전 이력 조회 (검색 필터)
     */
    public Page<TransactionInfoResponse> getChargingStats(LocalDate from, LocalDate to, String stationId, TransactionStatus status, Pageable pageable) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일은 조회 종료일 보다 앞서야 합니다");
        }
        return transactionRepository.findTransactionWithFilter(from, to, stationId, status, pageable);
    }

    /**
     * 6. 트랜젝션의 충전 프로파일 조회
     */
    public ChargingProfileResponse getChargingProfile(String transactionId) {
        return transactionRepository.findChargingProfileById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 트랜젝션이 존재하지 않습니다." + transactionId));
    }

    /**
     * 7. 최근 한달 매출, 전력, 사용횟수 조회
     * @param stationId: 비교할 충전소 1
     */
    public ChargingStatResponse getMonthlyChargingStats(String stationId) {

        List<ChargingDailyStat> actualResults = transactionRepository.getMonthlyChargingStatsByStationId(stationId);
        List<LocalDate> last30Days = getLastDaysList(29);
        return buildMonthlyStatResponse(actualResults, last30Days);
    }


    //== 메서드 ==//
    private static ChargingStatResponse buildMonthlyStatResponse(List<ChargingDailyStat> actualResults, List<LocalDate> last30Days) {
        Map<LocalDate, ChargingDailyStat> statMap = actualResults.stream()
                .collect(Collectors.toMap(ChargingDailyStat::getDate, stat -> stat));

        List<ChargingDailyStat> fullMonthData = new ArrayList<>();
        long totalCount = 0, totalRevenue = 0, totalEnergy = 0;

        for (LocalDate date : last30Days) {
            ChargingDailyStat stat = statMap.getOrDefault(date, new ChargingDailyStat(date, 0, 0, 0));
            totalCount += stat.getCount();
            totalRevenue += stat.getTotalRevenue();
            totalEnergy += stat.getTotalEnergy();
            fullMonthData.add(stat);
        }
        return new ChargingStatResponse(totalCount, totalRevenue, totalEnergy, fullMonthData);
    }

    private static WeeklyStatResponse buildWeeklyStatResponse(List<ChargingDailyStat> actualResults, List<LocalDate> last7Days) {
        Map<LocalDate, ChargingDailyStat> statMap = actualResults.stream()
                .collect(Collectors.toMap(ChargingDailyStat::getDate, stat -> stat));

        List<ChargingDailyStat> fullWeekData = new ArrayList<>();
        long totalCount = 0, totalRevenue = 0, totalEnergy = 0;

        for (LocalDate date : last7Days) {
            ChargingDailyStat stat = statMap.getOrDefault(date, new ChargingDailyStat(date, 0, 0, 0));
            totalCount += stat.getCount();
            totalRevenue += stat.getTotalRevenue();
            totalEnergy += stat.getTotalEnergy();
            fullWeekData.add(stat);
        }
        return new WeeklyStatResponse(totalCount, totalRevenue, totalEnergy, fullWeekData);
    }

    private static HourlyStatResponse buildHourlyStatResponse(String stationId, LocalDate searchingDate, List<ChargingHourlyStat> actualResults) {

        Map<String, ChargingHourlyStat> statMap = actualResults.stream()
                .collect(Collectors.toMap(ChargingHourlyStat::getHour, stat -> stat));

        List<String> hours = IntStream.range(0, 24)
                .mapToObj(i -> String.format("%02d", i))
                .toList();

        List<ChargingHourlyStat> fullHourData = new ArrayList<>();
        long totalCount = 0, totalRevenue = 0, totalEnergy = 0;

        for (String hour : hours) {
            ChargingHourlyStat stat = statMap.getOrDefault(hour, new ChargingHourlyStat(hour, 0, 0, 0));
            totalCount += stat.getCount();
            totalRevenue += stat.getTotalRevenue();
            totalEnergy += stat.getTotalEnergy();
            fullHourData.add(stat);
        }
        return new HourlyStatResponse(searchingDate, stationId, totalCount, totalRevenue, totalEnergy, fullHourData);
    }

    private static List<LocalDate> getLastDaysList(int endInclusive) {
        // 매출이 없는 경우 채워주는 로직
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return IntStream.rangeClosed(0, endInclusive)
                .mapToObj(today::minusDays)
                .sorted().toList();
    }
}
