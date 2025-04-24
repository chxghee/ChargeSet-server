package com.chargeset.chargeset_server.dto.tansaction;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargingUsageResponse {

    private long once;
    private long twice;
    private long thirdAndFourth;
    private long fifthOrMore;
}
