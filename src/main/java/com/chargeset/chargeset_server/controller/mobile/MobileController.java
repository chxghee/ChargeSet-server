package com.chargeset.chargeset_server.controller.mobile;

import com.chargeset.chargeset_server.dto.charging_station.ChargingStationInfo;
import com.chargeset.chargeset_server.dto.tansaction.ChargingDailyStat;
import com.chargeset.chargeset_server.service.ChargingStationService;
import com.chargeset.chargeset_server.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/mobile")
@AllArgsConstructor
public class MobileController {

    private final TransactionService transactionService;
    private final ChargingStationService chargingStationService;

    @GetMapping("/home")
    public String home(Model model) {

        List<ChargingStationInfo> chargingStationsLocation = chargingStationService.getChargingStationsInfo();
        model.addAttribute("chargingStations", chargingStationsLocation);


        model.addAttribute("activePage", "home");
        return "mobile/home";
    }

    @GetMapping("/reserve")
    public String mobileReserves(Model model) {
        model.addAttribute("activePage", "reserve");
        return "mobile/reserve";
    }
}
