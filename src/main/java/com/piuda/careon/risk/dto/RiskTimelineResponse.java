package com.piuda.careon.risk.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RiskTimelineResponse(

        UUID consultationId,
        LocalDateTime consultedAt,

        Integer riskScore,
        ConsultationStatus status,

        Integer currentRiskScore,
        Integer persistenceScore,
        Integer newChangeScore,

        List<String> aiTags,

        Boolean emergency

) {
}
