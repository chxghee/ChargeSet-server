package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.document.ChargingStation;
import com.chargeset.chargeset_server.dto.tansaction.ChargingStat;
import com.chargeset.chargeset_server.dto.tansaction.StationStatReport;
import com.chargeset.chargeset_server.dto.tansaction.TotalStatResponse;
import com.chargeset.chargeset_server.repository.charging_station.ChargingStationRepository;
import com.chargeset.chargeset_server.repository.transaction.TransactionRepository;
import com.chargeset.chargeset_server.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final ChargingStationRepository chargingStationRepository;

    /**
     * 1. 지난달, 충전소별 매출 집계 - 임시
     */
    public TotalStatResponse getMonthlyTotalReport(YearMonth month) {

        Pair<LocalDate, LocalDate> findMonth = TimeUtils.getMonthlyRangeByMonth(month);
        Pair<LocalDate, LocalDate> lastMonth = TimeUtils.getMonthlyRangeByMonth(month.minusMonths(1));

        List<ChargingStat> thisResults = transactionRepository.getChargingStatsReport(findMonth.getFirst(), findMonth.getSecond());
        List<ChargingStat> lastResults = transactionRepository.getChargingStatsReport(lastMonth.getFirst(), lastMonth.getSecond());

        // 1. map 변환
        Map<String, ChargingStat> findMonthStats = thisResults.stream()
                .collect(Collectors.toMap(ChargingStat::getStationId, stat -> stat));
        Map<String, ChargingStat> previousMonthStats = lastResults.stream()
                .collect(Collectors.toMap(ChargingStat::getStationId, stat -> stat));

        // 2. 이번달 저번달 충전 정보가 있는 Set
        Set<String> allStationIds = findAllStationIds();

        List<StationStatReport> stationStatReports = allStationIds.stream()
                .map(stationId -> createStationStatReport(stationId, findMonthStats, previousMonthStats)).toList();

        return createTotalStatResponse(stationStatReports, allStationIds, findMonth);
    }




    //== static 메서드 ==//
    private static TotalStatResponse createTotalStatResponse(List<StationStatReport> stationStatReports, Set<String> allStationIds, Pair<LocalDate, LocalDate> findMonth) {

        long totalCount = 0;
        long totalEnergy = 0;
        long totalRevenue = 0;
        long totalRevenueLastMonth = 0;

        for (StationStatReport report : stationStatReports) {
            totalCount += report.getTotalCount();
            totalEnergy += report.getTotalEnergy();
            totalRevenue += report.getTotalRevenue();
            totalRevenueLastMonth += report.getTotalRevenueLastMonth();
        }


        return new TotalStatResponse(
                allStationIds.size(),
                findMonth.getFirst(),
                findMonth.getSecond(),
                stationStatReports,
                totalCount,
                totalEnergy,
                totalRevenue,
                totalRevenueLastMonth
        );
    }


    private static StationStatReport createStationStatReport(String stationId, Map<String, ChargingStat> findMonthMap, Map<String, ChargingStat> lastMonthMap) {
        ChargingStat currentStat = findMonthMap.getOrDefault(stationId, new ChargingStat(stationId, 0, 0, 0));
        ChargingStat previousStat = lastMonthMap.getOrDefault(stationId, new ChargingStat(stationId, 0, 0, 0));

        return new StationStatReport(
                stationId,
                currentStat.getTotalCount(),
                currentStat.getTotalEnergy(),
                currentStat.getTotalRevenue(),
                previousStat.getTotalRevenue()
        );
    }

    private Set<String> findAllStationIds() {
        return chargingStationRepository.findAll().stream()
                .map(ChargingStation::getStationId)
                .collect(Collectors.toSet());
    }


}
