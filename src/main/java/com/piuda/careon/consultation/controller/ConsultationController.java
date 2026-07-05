package com.piuda.careon.consultation.controller;

import com.piuda.careon.consultation.dto.ConsultationResponse;
import com.piuda.careon.consultation.dto.CreateConsultationRequest;
import com.piuda.careon.consultation.service.ConsultationService;
import com.piuda.careon.consultation.dto.ConsultationDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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