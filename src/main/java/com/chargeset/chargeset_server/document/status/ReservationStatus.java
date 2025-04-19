package com.chargeset.chargeset_server.document.status;

public enum ReservationStatus {
    ACTIVE,     // 현재 유효한 예약. 아직 예약된 시간이 아님 (예약을 생성하면 ACTIVE 상태)
    WAITING,    // 예약된 시간이 되었지만 아직 연결하지 않음 (기다려주는 시간 10분이 아직 지나지 않음)
    ONGOING,    // 예약 시간에 도달했고, 예약자가 충전중
    EXPIRED,    // 예약 시간이 지났고, 실제 사용(충전)을 하지 않음 (노쇼)
    COMPLETED,  // 예약자가 충전을 정상적으로 수행했고, 충전이 완료됨
    CANCELED    // 사용자가 직접 예약을 취소함
}
