package com.piuda.careon.consultation.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ConsultationResponse(

        UUID id,
        String recipientName,
        Integer recipientAge,
        String caregiverName,
        LocalDateTime consultedAt,
        ConsultationStatus status,
        Integer riskScore,
        List<String> aiTags,
        String aiSummaryPreview

) {
}
