package com.piuda.careon.statistics.service;

import com.piuda.careon.statistics.domain.StatisticsPeriod;
import com.piuda.careon.statistics.dto.StatisticsDateRange;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
public class StatisticsPeriodResolver {

    public StatisticsDateRange resolve(
            StatisticsPeriod period,
            LocalDate customStartDate,
            LocalDate customEndDate
    ) {

        LocalDate today = LocalDate.now();

        return switch (period) {

            case WEEK -> resolveWeek(today);

            case MONTH -> resolveMonth(today);

            case THREE_MONTHS -> resolveThreeMonths(today);

            case SIX_MONTHS -> resolveSixMonths(today);

            case CUSTOM -> resolveCustom(
                    customStartDate,
                    customEndDate
            );
        };
    }

    private StatisticsDateRange resolveWeek(LocalDate today) {

        LocalDate startDate = today.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
        );

        LocalDate endDate = today.with(
                TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)
        );

        return new StatisticsDateRange(
                startDate,
                endDate
        );
    }

    private StatisticsDateRange resolveMonth(LocalDate today) {

        LocalDate startDate =
                today.withDayOfMonth(1);

        LocalDate endDate =
                today.with(
                        TemporalAdjusters.lastDayOfMonth()
                );

        return new StatisticsDateRange(
                startDate,
                endDate
        );
    }

    private StatisticsDateRange resolveThreeMonths(LocalDate today) {

        LocalDate startDate =
                today.minusMonths(2)
                        .withDayOfMonth(1);

        LocalDate endDate =
                today.with(
                        TemporalAdjusters.lastDayOfMonth()
                );

        return new StatisticsDateRange(
                startDate,
                endDate
        );
    }

    private StatisticsDateRange resolveSixMonths(LocalDate today) {

        LocalDate startDate =
                today.minusMonths(5)
                        .withDayOfMonth(1);

        LocalDate endDate =
                today.with(
                        TemporalAdjusters.lastDayOfMonth()
                );

        return new StatisticsDateRange(
                startDate,
                endDate
        );
    }

    private StatisticsDateRange resolveCustom(
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "직접 설정 기간에는 시작일과 종료일이 필요합니다."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "시작일은 종료일보다 늦을 수 없습니다."
            );
        }

        return new StatisticsDateRange(
                startDate,
                endDate
        );
    }
}
