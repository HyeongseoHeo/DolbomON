package com.piuda.careon.consultation.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConsultationResponse(
        UUID id,

        UUID recipientId,
        String recipientName,
        Integer recipientAge,

        UUID caregiverId,
        String caregiverName,

        LocalDateTime consultedAt,
        ConsultationStatus status,
        Integer riskScore,
        Boolean emergency,
        List<String> aiTags,
        String aiSummaryPreview

) {
}
