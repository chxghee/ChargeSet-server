package com.chargeset.chargeset_server.controller;

import com.chargeset.chargeset_server.dto.tansaction.TotalStatResponse;
import com.chargeset.chargeset_server.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;

    @GetMapping("/total-stats")
    public ResponseEntity<TotalStatResponse> totalStats(@RequestParam(name = "month", required = false)
                                                            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        if (month == null) {
            month = YearMonth.now().minusMonths(1);
        }
        return ResponseEntity.ok(reportService.getMonthlyTotalReport(month));
    }
}
