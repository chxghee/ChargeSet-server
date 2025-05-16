package com.chargeset.chargeset_server.document;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChargingSchedulePeriod {

    private int startPeriod;   // 초단위
    private int limit;         // 충전시 적용될 전력 (전력량이 아님)
    private boolean useESS;     // ESS를 사용한 충전인지 True 일 경우에는 비용이 0 이겠죠
}
