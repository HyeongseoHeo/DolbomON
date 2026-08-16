package com.piuda.careon.visitSchedule.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateVisitScheduleRequest(
        UUID caregiverId,
        UUID recipientId,
        LocalDateTime scheduledAt
) {
}
