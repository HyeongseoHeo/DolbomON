package com.piuda.careon.notification.dto;

import com.piuda.careon.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        UUID consultationId,
        UUID careRecipientId,
        Boolean isRead,
        LocalDateTime createdAt
) {
}
