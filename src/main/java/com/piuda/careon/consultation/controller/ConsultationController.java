package com.piuda.careon.consultation.controller;

import com.piuda.careon.consultation.dto.*;
import com.piuda.careon.consultation.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;

    @PostMapping
    public ResponseEntity<ConsultationResponse> createConsultation(
            @Valid @RequestBody CreateConsultationRequest request
    ) {
        ConsultationResponse response = consultationService.createConsultation(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ConsultationResponse>> getConsultations() {
        return ResponseEntity.ok(
                consultationService.getConsultations()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultationDetailResponse> getConsultation(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                consultationService.getConsultation(id)
        );
    }

    @PostMapping("/{id}/audio")
    public ResponseEntity<ConsultationDetailResponse> uploadAudio(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(
                consultationService.uploadAudio(id, file)
        );
    }

    @PatchMapping("/{id}/worker-note")
    public ResponseEntity<ConsultationDetailResponse> updateWorkerFinalNote(
            @PathVariable UUID id,
            @RequestBody UpdateWorkerFinalNoteRequest request
    ) {

        return ResponseEntity.ok(
                consultationService.updateWorkerFinalNote(
                        id,
                        request
                )
        );
    }

    @PatchMapping("/{id}/social-worker-opinion")
    public ResponseEntity<ConsultationDetailResponse> updateSocialWorkerOpinion(
            @PathVariable UUID id,
            @RequestBody UpdateSocialWorkerOpinionRequest request
    ) {

        return ResponseEntity.ok(
                consultationService.updateSocialWorkerOpinion(
                        id,
                        request
                )
        );
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<Void> sendFeedback(
            Authentication authentication,
            @PathVariable UUID id
    ) {

        consultationService.sendFeedback(
                authentication.getName(),
                id
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/process")
    public ResponseEntity<ConsultationDetailResponse> processConsultation(

            @RequestParam("caregiverId")
            UUID caregiverId,

            @RequestParam("recipientId")
            UUID recipientId,

            @RequestParam("consultedAt")
            String consultedAt,

            @RequestParam("file")
            MultipartFile file

    ) {
        return ResponseEntity.ok(

                consultationService.processConsultation(
                        caregiverId,
                        recipientId,
                        consultedAt,
                        file
                )
        );
    }
}