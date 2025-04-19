package com.chargeset.chargeset_server.dto.charging_station;

import com.chargeset.chargeset_server.document.status.EvseStatus;
import lombok.Data;

import java.util.EnumMap;
import java.util.List;

@Data
public class EvseStatusSummary {

    private int available;
    private int charging;
    private int faulted;
    private int reserved;
    
    public EvseStatusSummary(List<EvseStatusCount> evseStatusCounts) {

        EnumMap<EvseStatus, Integer> countMap = new EnumMap<>(EvseStatus.class);

        for (EvseStatus status : EvseStatus.values()) {
            countMap.put(status, 0);
        }
        for (EvseStatusCount count : evseStatusCounts) {
            try {
                EvseStatus status = EvseStatus.valueOf(count.getStatus());
                countMap.put(status, count.getCount());
            } catch (IllegalArgumentException | NullPointerException ignored) {
                // 잘못된 status 값은 무시
            }
        }

        this.available = countMap.get(EvseStatus.AVAILABLE);
        this.charging = countMap.get(EvseStatus.CHARGING);
        this.faulted = countMap.get(EvseStatus.FAULTED);
        this.reserved = countMap.get(EvseStatus.RESERVED);
    }
}
