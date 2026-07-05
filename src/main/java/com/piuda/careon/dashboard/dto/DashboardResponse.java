package com.piuda.careon.dashboard.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardResponse(

        long totalConsultations,

        long normalCount,

        long needReviewCount,

        long specialNoteCount,

        double averageRiskScore,

        long highRiskCount,

        List<TagStatistic> topTags

) {
}


