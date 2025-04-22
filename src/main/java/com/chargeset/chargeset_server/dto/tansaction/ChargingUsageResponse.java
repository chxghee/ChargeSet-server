package com.chargeset.chargeset_server.dto.tansaction;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChargingUsageResponse {

    private long once;
    private long twice;
    private long thirdAndFourth;
    private long fifthOrMore;
}
