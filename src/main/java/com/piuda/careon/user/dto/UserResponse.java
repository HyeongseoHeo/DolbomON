package com.piuda.careon.user.dto;

import com.piuda.careon.user.entity.EmploymentStatus;
import com.piuda.careon.user.entity.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String phone,
        String email,
        UserRole role,
        Boolean isActive,
        EmploymentStatus employmentStatus,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
}