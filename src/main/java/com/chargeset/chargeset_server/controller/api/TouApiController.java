package com.chargeset.chargeset_server.controller.api;

import com.chargeset.chargeset_server.dto.TouInfoResponse;
import com.chargeset.chargeset_server.service.TouService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tou")
@RequiredArgsConstructor
public class TouApiController {

    private final TouService touService;

    @GetMapping
    public ResponseEntity<TouInfoResponse> touInfo() {
        return ResponseEntity.ok(touService.getTouInfo());
    }
}
