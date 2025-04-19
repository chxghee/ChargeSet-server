package com.chargeset.chargeset_server.document;

import lombok.Data;

@Data
public class ChargingSchedulePeriod {

    private int startPeriod;
    private int limit;         // 충전시 적용될 전력 (전력량이 아님)
    private boolean useESS;     // ESS를 사용한 충전인지 True 일 경우에는 비용이 0 이겠죠
}
