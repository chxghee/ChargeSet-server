package com.chargeset.chargeset_server.document.status;

public enum TransactionStatus {
    COMPLETED,      // 정상 종료
    ABORTED,        // CSMS 강제 중단
    FAILED,         // 전력 문제 등 실패
    INTERRUPTED,     // 사용자가 중간에 커넥터 뽑음 (EVDisconnected)
    CHARGING
}
