package com.piuda.careon.auth.dto;

import com.piuda.careon.user.entity.UserRole;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        UUID userId,
        String name,
        String email,
        UserRole role,
        UUID institutionId,
        String institutionCode,
        String institutionName
) {
}