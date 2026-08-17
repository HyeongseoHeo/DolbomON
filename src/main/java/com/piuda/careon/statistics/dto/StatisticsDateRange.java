package com.piuda.careon.statistics.dto;

import java.time.LocalDate;

public record StatisticsDateRange(
        LocalDate startDate,
        LocalDate endDate
) {
}
