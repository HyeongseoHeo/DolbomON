package com.piuda.careon.careRecipient.controller;

import com.piuda.careon.careRecipient.dto.CareRecipientResponse;
import com.piuda.careon.careRecipient.dto.CreateCareRecipientRequest;
import com.piuda.careon.careRecipient.dto.UpdateCareRecipientRequest;
import com.piuda.careon.careRecipient.service.CareRecipientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/care-recipients")
public class CareRecipientController {

    private final CareRecipientService careRecipientService;

    @PostMapping
    public CareRecipientResponse create(
            Authentication authentication,
            @RequestBody CreateCareRecipientRequest request
    ) {
        String adminEmail = authentication.getName();

        return careRecipientService.create(
                adminEmail,
                request
        );
    }

    @GetMapping("/{id}")
    public CareRecipientResponse getRecipient(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        return careRecipientService.getRecipient(
                authentication.getName(),
                id
        );
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<CareRecipientResponse> getRecipientsByCaregiver(
            Authentication authentication,
            @PathVariable UUID caregiverId
    ) {
        return careRecipientService.getRecipientsByCaregiver(
                authentication.getName(),
                caregiverId
        );
    }

    @DeleteMapping("/{id}")
    public void delete(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        careRecipientService.delete(
                authentication.getName(),
                id
        );
    }

    @GetMapping
    public List<CareRecipientResponse> getRecipients(
            Authentication authentication
    ) {

        String userEmail = authentication.getName();

        return careRecipientService
                .getInstitutionRecipients(userEmail);
    }

    @PutMapping("/{id}")
    public CareRecipientResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @RequestBody UpdateCareRecipientRequest request
    ) {

        String userEmail = authentication.getName();

        return careRecipientService.update(
                userEmail,
                id,
                request
        );
    }
}
