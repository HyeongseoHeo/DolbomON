package com.piuda.careon.auth.dto;

public record LoginRequest(
        String institutionCode,
        String email,
        String password
) {
}