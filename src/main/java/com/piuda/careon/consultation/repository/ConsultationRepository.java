package com.piuda.careon.consultation.repository;

import com.piuda.careon.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {

    List<Consultation> findTop3ByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);
    List<Consultation> findAllByOrderByConsultedAtDesc();

    Optional<Consultation> findTopByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);

    List<Consultation> findByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);
    List<Consultation> findTop2ByRecipient_IdOrderByConsultedAtDesc(UUID recipientId);
}
