package com.piuda.careon.careRecipient.controller;

import com.piuda.careon.careRecipient.dto.CareRecipientResponse;
import com.piuda.careon.careRecipient.dto.CreateCareRecipientRequest;
import com.piuda.careon.careRecipient.service.CareRecipientService;
import lombok.RequiredArgsConstructor;
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
            @RequestBody CreateCareRecipientRequest request
    ) {
        return careRecipientService.create(request);
    }

    @GetMapping("/{id}")
    public CareRecipientResponse getRecipient(
            @PathVariable UUID id
    ) {
        return careRecipientService.getRecipient(id);
    }

    @GetMapping("/caregiver/{caregiverId}")
    public List<CareRecipientResponse> getRecipientsByCaregiver(
            @PathVariable UUID caregiverId
    ) {
        return careRecipientService.getRecipientsByCaregiver(caregiverId);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {
        careRecipientService.delete(id);
    }
}
