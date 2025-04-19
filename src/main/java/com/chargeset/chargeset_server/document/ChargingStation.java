package com.chargeset.chargeset_server.document;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "chargingStation")
@Getter
public class ChargingStation {

    @Id
    private String id;

    private String stationId;             // 내부 시스템 식별자
    private String stationName;
    private Location location;
    private Instant lastHeartbeat;        // 최근 하트비트 수신 시각
}
