package com.chargeset.chargeset_server.document;

import com.chargeset.chargeset_server.document.status.TransactionStatus;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "transaction")
@Getter
public class Transaction {

    @Id
    private String id;

    // 충전소 관련
    private String stationId;     // 충전소 ID (ex: ST-001)
    private String evseId;        // EVSE (충전기) ID
    private int connectorId;      // 커넥터 번호 (1, 2...)

    // 사용자 관련
    private String userId;
    private String idToken;

    private String reservationId;

    private Instant startTime;
    private Instant endTime;

    private int energyWh;
    private int cost;

    private TransactionStatus transactionStatus;

    // 충전 프로파일 관련 스냅샷
    private Instant startSchedule;
    private List<ChargingSchedulePeriod> chargingProfileSnapshots;
}
