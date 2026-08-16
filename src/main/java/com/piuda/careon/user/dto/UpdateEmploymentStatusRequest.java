package com.piuda.careon.user.dto;

import com.piuda.careon.user.entity.EmploymentStatus;

public record UpdateEmploymentStatusRequest(
        EmploymentStatus employmentStatus
) {
}
