package com.piuda.careon.consultation.repository;

import com.piuda.careon.consultation.entity.Consultation;
import com.piuda.careon.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {

    List<Consultation> findTop3ByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);
    List<Consultation> findAllByOrderByConsultedAtDesc();

    Optional<Consultation> findTopByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);

    List<Consultation> findByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);
    List<Consultation> findTop2ByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);

    List<Consultation> findByConsultedAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<Consultation> findByCaregiver_InstitutionAndConsultedAtBetween(
            Institution institution,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Consultation>
    findTopByRecipient_IdAndConsultedAtBeforeOrderByConsultedAtDesc(
            UUID recipientId,
            LocalDateTime before
    );
}
