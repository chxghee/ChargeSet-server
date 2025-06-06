package com.chargeset.chargeset_server.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewReservationRequest {

    private LocalDate startDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    private int chargingMinute;       // 분단위
    private int targetEnergyWh;

}
