package com.chargeset.chargeset_server.dto.tansaction;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingHourlyStat {
    private String hour;        // "00:00" ~ "23:00"
    private long count;          // 건수
    private long totalEnergy;        // 전력량
    private long totalRevenue;        // 전력량
}
