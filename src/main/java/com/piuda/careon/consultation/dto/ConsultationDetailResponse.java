package com.piuda.careon.consultation.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConsultationDetailResponse(
        UUID id,
        String recipientName,
        Integer recipientAge,
        String caregiverName,
        LocalDateTime consultedAt,
        String audioUrl,

        ConsultationStatus status,

        Integer riskScore,

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
        String sttText,
        String aiSummary,
        String aiSummaryPreview,
        String workerFinalNote,
        String socialWorkerOpinion
) {
}


