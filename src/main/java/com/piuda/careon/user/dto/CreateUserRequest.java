package com.piuda.careon.user.dto;

import com.piuda.careon.user.entity.UserRole;

public record CreateUserRequest(

        String name,
        String phone,
        String email,
        String password,
        UserRole role

) {
}
