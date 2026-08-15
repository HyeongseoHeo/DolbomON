package com.piuda.careon.risk.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RiskChangeResponse(

        UUID recipientId,

        Integer currentRiskScore,
        Integer previousRiskScore,
        Integer scoreChange,

        String trend,

        List<String> newTags,
        List<String> resolvedTags,
        List<String> persistentTags,

        LocalDateTime currentConsultedAt,
        LocalDateTime previousConsultedAt

) {
}
