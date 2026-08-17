package com.piuda.careon.statistics.dto;

import com.piuda.careon.statistics.domain.StatisticsPeriod;

import java.time.LocalDate;
import java.util.List;

public record StatisticsSummaryResponse(
        StatisticsPeriod period,
        LocalDate startDate,
        LocalDate endDate,

        long completedVisits,          // 방문 완료
        long consultationCount,        // 상담일지 작성
        long abnormalDetectionCount,   // 이상징후 탐지
        long riskTransitionCount,      // 위험군 건수
        List<AbnormalTrendItem> abnormalTrend
) {
}
