package com.piuda.careon.auth.dto;

import java.util.UUID;

public record CheckInstitutionResponse(
        UUID institutionId,
        String institutionCode,
        String institutionName
) {
}
