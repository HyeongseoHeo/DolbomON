package com.piuda.careon.consultation.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConsultationDetailResponse(
        UUID id,

        UUID recipientId,
        String recipientName,
        Integer recipientAge,

        UUID caregiverId,
        String caregiverName,

        LocalDateTime consultedAt,
        String audioUrl,

        ConsultationStatus status,

        Integer riskScore,
        Integer previousRiskScore,
        Integer riskScoreChange,

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


