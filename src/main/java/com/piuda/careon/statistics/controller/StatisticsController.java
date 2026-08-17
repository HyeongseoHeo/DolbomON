package com.piuda.careon.statistics.controller;

import com.piuda.careon.statistics.domain.StatisticsPeriod;
import com.piuda.careon.statistics.dto.StatisticsSummaryResponse;
import com.piuda.careon.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping
    public ResponseEntity<StatisticsSummaryResponse> getStatistics(
            Authentication authentication,

            @RequestParam StatisticsPeriod period,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {

        return ResponseEntity.ok(
                statisticsService.getSummary(
                        authentication.getName(),
                        period,
                        startDate,
                        endDate
                )
        );
    }
}
