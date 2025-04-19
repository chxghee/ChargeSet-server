package com.chargeset.chargeset_server.document.status;

public enum EvseStatus {
    AVAILABLE,      // 대기중
    CHARGING,       // 충전중
    FAULTED,        // 고장
    RESERVED;       // 현재 누군가가 예약한 시간인 상황 (예약시간에 아직 오지 않았다면 이 상테겠죠)
}
