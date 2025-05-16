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

    private final TouRepository touRepository;
    private final EvseRepository evseRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public NewReservation calcFee(NewReservationRequest request, String idToken, String stationId) {

        // 1. 사용자 유효성 검사 - 토큰 아이디 검사
        User user = userRepository.findByIdToken(idToken)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자가 아닙니다!"));

        // 입력받은 시작 시간으로 endTime 계산
        LocalDateTime startTime = request.getStartDate().atTime(request.getStartTime());
        LocalDateTime endTime = startTime.plusMinutes(request.getChargingMinute());

        // 2. 해당 시기에 예약 가능한지 -> 해당 시간동안 예약 테이블 확인하여 예약 가능한 evse 찾기
        List<EvseIdOnly> availableEvseIds = findAvailableEvseIds(stationId, startTime, endTime);

        if (availableEvseIds.isEmpty()) {
            throw new IllegalArgumentException("현재 예약 가능한 충전기가 없습니다!");
        }


        // 3. 요청 유효성 검사 - 가능한 요청인지? -> 만약 1시간 예약 했는데 10000kWh 충전 요청이 온다면? IllegalArgumentException("~~~오류 메세지") 로 예외를 던저 주세요 -> 일단 되면 좋고 안되면 어쩔수 없죠


        // 4. 충전 프로파일 산출 -> 충전 프로파일은 NewChargingProfile 객체를 생성해야 함
        NewChargingProfile chargingProfile = getChargingProfile(startTime);


        // 5. 충전 비용 산출 -> TOU 와 ESS 뭐시기로 암튼 비용을 산정해주세요
        // -> 일단 아직 DB에 저장될 ESS 관련 데이터 스키마를 정하지 않아서 이거 만들떄 편한 형태로 정해주면 그대로 저장하도록 하겠음
        int cost = getCost();


        // 일단 이런식으로 이루어 질거 같은데 코드를 짜면서 로직 흐름은 수정해도 됨
        // 중요한건 마지막 리턴되는 객체에 맞게 데이터를 잘 넣어 주어야 합니다.



        return new NewReservation(
                stationId,
                availableEvseIds.getFirst().getEvseId(),
                user.getId(),
                idToken,
                TimeUtils.convertDateTimeToUTC(startTime),
                TimeUtils.convertDateTimeToUTC(endTime),
                request.getTargetEnergyWh(),
                cost,
                chargingProfile
        );        // 마지막으로 생성된 내용을 종합해서 NewReservation 객체를 생성해 주면 됨
    }

    // 이 부분 구현해 주세요
    private NewChargingProfile getChargingProfile(LocalDateTime startTime) {
        List<ChargingSchedulePeriod> periods = new ArrayList<>();
        periods.add(new ChargingSchedulePeriod(0, 3000, false));
        periods.add(new ChargingSchedulePeriod(1800, 2000, true));
        periods.add(new ChargingSchedulePeriod(3600, 5000, true));

        return new NewChargingProfile(TimeUtils.convertDateTimeToUTC(startTime), periods);
    }

    // 이 부분 구현해 주세요
    private int getCost() {
        List<Tou> touData = touRepository.findAll();    // TOU 데이터를 가져올 수 있음
        return 20000;
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


}
