package com.chargeset.chargeset_server.repository.transaction;

import com.chargeset.chargeset_server.document.status.TransactionStatus;
import com.chargeset.chargeset_server.dto.tansaction.ChargingDailyStat;
import com.chargeset.chargeset_server.dto.tansaction.ChargingHourlyStat;
import com.chargeset.chargeset_server.dto.tansaction.ChargingProfileResponse;
import com.chargeset.chargeset_server.dto.tansaction.TransactionInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionCustomRepository {

    Optional<ChargingDailyStat> getTodayChargingStats();

    Optional<ChargingDailyStat> getTodayChargingStatsByStationId(String stationId);

    List<ChargingDailyStat> getWeeklyChargingStats();

    List<ChargingHourlyStat> getHourlyChargingStats(String stationId, LocalDate searchingDate);

    Page<TransactionInfoResponse> findTransactionWithFilter(LocalDate from, LocalDate to, String stationId,
                                                            TransactionStatus status, Pageable pageable);

    Optional<ChargingProfileResponse> findChargingProfileById(String id);

    List<ChargingDailyStat> getMonthlyChargingStatsByStationId(String stationId);
}
