package com.piuda.careon.careRecipient.dto;

import com.piuda.careon.careRecipient.entity.CareLevel;
import com.piuda.careon.careRecipient.entity.Gender;

import java.util.UUID;

public record CreateCareRecipientRequest(
        String name,
        Integer age,
        Gender gender,
        String address,
        CareLevel careLevel,
        String mainDisease,
        String phone,
        String familyContactName,
        String familyRelation,
        String familyContactPhone,
        String memo,
        UUID caregiverId
) {
}
