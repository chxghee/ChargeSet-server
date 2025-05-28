package com.chargeset.chargeset_server.service;

import com.chargeset.chargeset_server.document.ChargingSchedulePeriod;
import com.chargeset.chargeset_server.document.Tou;
import com.chargeset.chargeset_server.document.User;
import com.chargeset.chargeset_server.dto.EvseIdOnly;
import com.chargeset.chargeset_server.dto.reservation.NewChargingProfile;
import com.chargeset.chargeset_server.dto.reservation.NewReservation;
import com.chargeset.chargeset_server.dto.reservation.NewReservationRequest;
import com.chargeset.chargeset_server.repository.UserRepository;
import com.chargeset.chargeset_server.repository.evse.EvseRepository;
import com.chargeset.chargeset_server.repository.reservation.ReservationRepository;
import com.chargeset.chargeset_server.repository.tou.TouRepository;
import com.chargeset.chargeset_server.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingCostService {

    private final EvseRepository evseRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public NewReservation calculateFee(NewReservationRequest request, String idToken, String stationId) {

        // 1. 사용자 유효성 검사 - 토큰 아이디 검사
        User user = userRepository.findByIdToken(idToken)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자가 아닙니다!"));

        // 입력받은 시작 시간으로 endTime 계산
        LocalDateTime startTime = request.getStartDate().atTime(request.getStartTime());
        LocalDateTime endTime = startTime.plusMinutes(request.getChargingMinute());

        // 2. 해당 시기에 예약 가능한지 -> 해당 시간동안 예약 테이블 확인하여 예약 가능한 evse 찾기
        List<EvseIdOnly> availableEvseIds = validateChargingRequest(stationId, startTime, endTime);

        int chargingMinute = request.getChargingMinute();
        int chargingWh = request.getTargetEnergyWh();

        //  완속은 시간당 6000(분당 100)Wh, 고속은 시간당 60000(분당 1000)Wh 충전
        if (chargingMinute * 1000 < chargingWh) {
            throw new IllegalArgumentException("요구 충전량을 충전하기 위한 시간이 충분하지 않습니다!");
        }


        ScheduleAndCost scheduleAndCost = calculateScheduleAndCost(chargingMinute, chargingWh, stationId);

        NewChargingProfile chargingProfile = getChargingProfile(scheduleAndCost, startTime);


        return new NewReservation(
                stationId,
                availableEvseIds.getFirst().getEvseId(),
                user.getId(),
                idToken,
                TimeUtils.convertDateTimeToUTC(startTime),
                TimeUtils.convertDateTimeToUTC(endTime),
                request.getTargetEnergyWh(),
                scheduleAndCost.getCost(),
                chargingProfile
        );
    }

    private List<EvseIdOnly> validateChargingRequest(String stationId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("과거 시각으로는 예약할 수 없습니다!");
        }
        List<EvseIdOnly> availableEvseIds = findAvailableEvseIds(stationId, startTime, endTime);
        if (availableEvseIds.isEmpty()) {
            throw new IllegalArgumentException("현재 예약 가능한 충전기가 없습니다!");
        }
        return availableEvseIds;
    }

    /**
     * 스케줄  계산 식
     * 100 * standardChargingMinute + 1000 * fastChargingMinute = chargingWh
     * standardChargingMinute + fastChargingMinute = chargingMinute
     */
    private ScheduleAndCost calculateScheduleAndCost(int chargingMinute, int chargingWh, String stationId) {
        StationChargingCost chargingCost = StationChargingCost.from(stationId);

        double fastChargingMinute = (chargingWh - 100.0 * chargingMinute) / 900.0;
        double standardChargingMinute = chargingMinute - fastChargingMinute;
        int cost = (int) Math.round(
                standardChargingMinute * chargingCost.getStandardChargingCostPerMinute() +
                        fastChargingMinute * chargingCost.getFastChargingCostPerMinute()
        );
        return new ScheduleAndCost(cost, standardChargingMinute, fastChargingMinute);
    }

    private NewChargingProfile getChargingProfile(ScheduleAndCost scheduleAndCost, LocalDateTime startTime) {
        List<ChargingSchedulePeriod> periods = new ArrayList<>();
        int standardSecond = (int) Math.round(scheduleAndCost.getStandardChargingMinute() * 60);
        int endSecond = standardSecond + (int) Math.round(scheduleAndCost.getFastChargingMinute() * 60);

        periods.add(new ChargingSchedulePeriod(0, 6000, false));
        periods.add(new ChargingSchedulePeriod(standardSecond, 60000, true));
        periods.add(new ChargingSchedulePeriod(endSecond, 0, false));

        return new NewChargingProfile(TimeUtils.convertDateTimeToUTC(startTime), periods);
    }

    private List<EvseIdOnly> findAvailableEvseIds(String stationId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 해당 시간대 예약중인 evse 찾는 쿼리
        List<EvseIdOnly> occupiedEvseIds = reservationRepository.findOccupiedEvseIdsReservedAt(stationId, startTime, endTime);
        List<String> occupiedEvseIdsList = occupiedEvseIds.stream()
                .map(EvseIdOnly::getEvseId)
                .toList();

        // 2. 해당 시간 대 예약 가능한 evse 찾아 반환
        return evseRepository.findAvailableEvseIds(occupiedEvseIdsList, stationId);
    }


    @Getter
    @AllArgsConstructor
    static class ScheduleAndCost {
        private int cost;
        private double standardChargingMinute;
        private double fastChargingMinute;
    }
}
