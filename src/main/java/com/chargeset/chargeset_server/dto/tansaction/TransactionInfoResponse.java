package com.chargeset.chargeset_server.dto.tansaction;

import com.chargeset.chargeset_server.document.status.TransactionStatus;
import lombok.Data;

@Data
public class TransactionInfoResponse {

    private String startTime;
    private String endTime;
    private String id;
    private String stationId;     // 충전소 ID (ex: ST-001)
    private String evseId;        // EVSE (충전기) ID
    private String userId;
    private int energyWh;
    private int cost;
    private TransactionStatus status;
}
