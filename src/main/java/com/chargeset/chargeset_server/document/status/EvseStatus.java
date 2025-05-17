package com.chargeset.chargeset_server.document.status;

public enum EvseStatus {
    AVAILABLE,      // 대기중
    CHARGING,       // 충전중
    FAULTED,        // 고장
    RESERVED,       // 현재 누군가가 예약한 시간인 상황 (예약시간에 아직 오지 않았다면 이 상태)
    OFFLINE;        // 충전 머신이 오프라인 상태(켜지지 않은 상태)
}
