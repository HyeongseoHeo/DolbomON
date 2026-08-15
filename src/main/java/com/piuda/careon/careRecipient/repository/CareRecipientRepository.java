package com.piuda.careon.careRecipient.repository;

import com.piuda.careon.careRecipient.entity.CareRecipient;
import com.piuda.careon.institution.entity.Institution;
import com.piuda.careon.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CareRecipientRepository
        extends JpaRepository<CareRecipient, UUID> {

    List<CareRecipient> findByCaregiver(User caregiver);

    List<CareRecipient> findByInstitutionOrderByCreatedAtDesc(
            Institution institution
    );

    Optional<CareRecipient> findByName(String name);
}
