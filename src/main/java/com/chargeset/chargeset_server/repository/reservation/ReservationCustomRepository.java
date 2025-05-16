package com.chargeset.chargeset_server.repository.reservation;

import com.chargeset.chargeset_server.document.status.ReservationStatus;
import com.chargeset.chargeset_server.dto.EvseIdOnly;
import com.chargeset.chargeset_server.dto.reservation.ReservationInfoResponse;
import com.chargeset.chargeset_server.dto.reservation.ReservationNoShowCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationCustomRepository {

    Page<ReservationInfoResponse> findTodayReservations(Pageable pageable);

    Page<ReservationInfoResponse> findReservationsWithFilter(LocalDate from, LocalDate to, String stationId,
                                                             ReservationStatus status, Pageable pageable);

    List<ReservationInfoResponse> findAllReservationsByStationIdAndStartTime(String stationId, LocalDate from, LocalDate to);

    List<ReservationNoShowCount> getNoShowCounts(LocalDate from, LocalDate to);

    List<EvseIdOnly> findOccupiedEvseIdsReservedAt(String stationId, LocalDateTime from, LocalDateTime to);

    Optional<ReservationInfoResponse> findCloseReservationByUserId(String userId);
}
