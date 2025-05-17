package com.chargeset.chargeset_server.controller.mobile;

import com.chargeset.chargeset_server.document.User;
import com.chargeset.chargeset_server.repository.UserRepository;
import com.chargeset.chargeset_server.service.ChargingStationService;
import com.chargeset.chargeset_server.service.TransactionService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/mobile")
@AllArgsConstructor
public class MobileController {

    private final TransactionService transactionService;
    private final ChargingStationService chargingStationService;
    private final UserRepository userRepository;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("chargingStations", chargingStationService.getChargingStationsInfo());

        model.addAttribute("activePage", "home");
        return "mobile/home";
    }

    @GetMapping("/reserve")
    public String mobileReserves(Model model) {
        model.addAttribute("chargingStations", chargingStationService.getChargingStationsInfo());
        model.addAttribute("activePage", "reserve");
        return "mobile/reserve";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "mobile/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "email") String email, @RequestParam(name = "password") String password,
                        HttpSession session, RedirectAttributes redirectAttributes) {

        Optional<User> findUser = userRepository.findByEmail(email);
        if (findUser.isEmpty() || !password.equals(findUser.get().getPassword())) {
            redirectAttributes.addFlashAttribute("message", "아이디와 비밀번호가 일치하지 않습니다.");
            return "redirect:/mobile/login";
        }

        session.setAttribute("user", findUser.get());
        return "redirect:/mobile/home";
    }
}
