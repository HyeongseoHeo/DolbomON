package com.piuda.careon.user.dto;

public record ResetPasswordResponse(
        String temporaryPassword,
        boolean mustChangePassword
) {
}
