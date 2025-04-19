package com.chargeset.chargeset_server.dto.charging_station;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvseStatusCount {
    private String status;
    private int count;
}
