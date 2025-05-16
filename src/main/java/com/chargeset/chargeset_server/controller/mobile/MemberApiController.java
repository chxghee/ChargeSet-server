package com.chargeset.chargeset_server.controller.mobile;

import com.chargeset.chargeset_server.dto.reservation.NewReservation;
import com.chargeset.chargeset_server.dto.reservation.NewReservationRequest;
import com.chargeset.chargeset_server.dto.reservation.ReservationInfoResponse;
import com.chargeset.chargeset_server.service.ChargingCostService;
import com.chargeset.chargeset_server.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final ChargingCostService chargingCostService;
    private final ReservationService reservationService;


    @PostMapping("/{idToken}/reservations/{stationId}/recommend")
    public ResponseEntity<NewReservation> getReserveInfo(@PathVariable("idToken") String idToken,
                                                  @PathVariable("stationId") String stationId,
                                                  @RequestBody NewReservationRequest request) {
        return ResponseEntity.ok(chargingCostService.calcFee(request, idToken, stationId));
    }

    @PostMapping("/reservations/confirm")
    public ResponseEntity<Void> reserve(@RequestBody NewReservation request) {
        reservationService.saveReservation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/reservations")
    public ResponseEntity<ReservationInfoResponse> getCloseReservation(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(reservationService.getCloseReservation(userId));
    }

}
