package com.chargeset.chargeset_server.dto.tansaction;

import com.chargeset.chargeset_server.document.ChargingSchedulePeriod;
import com.chargeset.chargeset_server.document.Transaction;
import com.chargeset.chargeset_server.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChargingProfileResponse {

    private String transactionId;
    private int energyWh;
    private int cost;
    private String startTime;
    private String endTime;
    private String startSchedule;
    private List<ChargingSchedulePeriod> chargingProfileSnapshots;

    public ChargingProfileResponse(Transaction transaction) {
        this.transactionId = transaction.getId();
        this.energyWh = transaction.getEnergyWh();
        this.cost = transaction.getCost();
        this.startTime = TimeUtils.formatInstantToKSTString(transaction.getStartTime());
        this.endTime = TimeUtils.formatInstantToKSTString(transaction.getEndTime());
        this.startSchedule = TimeUtils.formatInstantToKSTString(transaction.getStartSchedule());
        this.chargingProfileSnapshots = transaction.getChargingProfileSnapshots();
    }
}
