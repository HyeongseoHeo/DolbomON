package com.piuda.careon.auth.dto;

import com.piuda.careon.user.entity.UserRole;

public record SignupRequest(
        String institutionCode,
        String name,
        String phone,
        String email,
        String password,
        UserRole role,
        Boolean agreedTerms
) {
}
