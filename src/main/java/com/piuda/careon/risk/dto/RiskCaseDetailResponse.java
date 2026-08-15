package com.piuda.careon.risk.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RiskCaseDetailResponse(

        UUID recipientId,
        String recipientName,
        Integer recipientAge,

        UUID caregiverId,
        String caregiverName,

        Integer riskScore,
        ConsultationStatus status,

        Integer currentRiskScore,
        Integer persistenceScore,
        Integer newChangeScore,

        Integer nutritionScore,
        Integer mentalEmotionalScore,
        Integer cognitiveCommunicationScore,
        Integer physicalFunctionalSafetyScore,
        Integer socialSupportScore,

        Boolean emergency,

        List<String> aiTags,

        LocalDateTime lastConsultedAt,

        String aiSummary,
        String socialWorkerOpinion

) {
}
