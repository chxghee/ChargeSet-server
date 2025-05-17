package com.chargeset.chargeset_server.controller;

import com.chargeset.chargeset_server.dto.charging_station.ChargingStationInfo;
import com.chargeset.chargeset_server.dto.tansaction.ChargingDailyStat;
import com.chargeset.chargeset_server.service.ChargingStationService;
import com.chargeset.chargeset_server.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TransactionService transactionService;
    private final ChargingStationService chargingStationService;

    @GetMapping("/")
    public String home(Model model) {
        List<ChargingStationInfo> chargingStationsLocation = chargingStationService.getChargingStationsInfo();

        model.addAttribute("stationCount", chargingStationsLocation.size());
        model.addAttribute("chargingStations", chargingStationsLocation);
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/station/{stationId}")
    public String stations(@PathVariable(name = "stationId") String stationId, Model model) {

        ChargingStationInfo chargingStationInfo = chargingStationService.getChargingStationInfo(stationId);

        model.addAttribute("activePage", "station");
        model.addAttribute("stationId", stationId);
        model.addAttribute("chargingStationInfo", chargingStationInfo);

        return "station";
    }

    @GetMapping("/transaction")
    public String transactions(Model model) {
        List<ChargingStationInfo> chargingStationsLocation = chargingStationService.getChargingStationsInfo();
        model.addAttribute("chargingStations", chargingStationsLocation);
        model.addAttribute("activePage", "transaction");
        return "transaction";
    }

    @GetMapping("/reservation")
    public String reservations(Model model) {
        model.addAttribute("activePage", "reservation");
        return "reservation";
    }

    @GetMapping("/report")
    public String reports(Model model) {
        model.addAttribute("activePage", "report");
        return "report";
    }


}
