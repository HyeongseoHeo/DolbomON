package com.piuda.careon.risk.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RiskRecipientResponse(
        UUID recipientId,
        String recipientName,
        Integer recipientAge,

        UUID caregiverId,
        String caregiverName,

        List<String> aiTags,

        Integer riskScore,
        ConsultationStatus status,
        Boolean emergency,

        LocalDateTime lastConsultedAt
) {
}
