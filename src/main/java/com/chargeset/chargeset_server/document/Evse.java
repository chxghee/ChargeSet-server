package com.chargeset.chargeset_server.document;

import com.chargeset.chargeset_server.document.status.EvseStatus;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@Document(collection = "evse")
@Getter
public class Evse {

    @Id
    private String id;
    private String evseId;
    private int connectorId;
    private String powerType;         //  DC -> 삭제 가능성 있음
    private int voltage;              // 정격 전압 (V)
    private int currentA;             // 정격 전류 (A)
    private EvseStatus status;
    private Instant lastUpdated;
    private String stationId;

    public Evse(String evseId, int connectorId, String powerType, int voltage, int currentA, EvseStatus status, Instant lastUpdated, String stationId) {
        this.evseId = evseId;
        this.connectorId = connectorId;
        this.powerType = powerType;
        this.voltage = voltage;
        this.currentA = currentA;
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.stationId = stationId;
    }
}
