package com.piuda.careon.visitSchedule.dto;

import com.piuda.careon.visitSchedule.entity.VisitScheduleStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VisitScheduleResponse(
        UUID id,
        UUID caregiverId,
        String caregiverName,
        UUID recipientId,
        String recipientName,
        LocalDateTime scheduledAt,
        VisitScheduleStatus status,
        Boolean reminderSent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
