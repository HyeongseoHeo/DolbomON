package com.piuda.careon.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateConsultationRequest(

        @NotBlank
        String recipientName,

        Integer recipientAge,

        @NotNull
        UUID caregiverId,

        @NotNull
        LocalDateTime consultedAt,

        String audioUrl

) {
}