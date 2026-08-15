package com.piuda.careon.risk.controller;

import com.piuda.careon.risk.dto.RiskCaseDetailResponse;
import com.piuda.careon.risk.dto.RiskChangeResponse;
import com.piuda.careon.risk.dto.RiskRecipientResponse;
import com.piuda.careon.risk.dto.RiskTimelineResponse;
import com.piuda.careon.risk.service.RiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risk")
public class RiskController {

    private final RiskService riskService;

    @GetMapping
    public List<RiskRecipientResponse> getRiskRecipients(
            Authentication authentication
    ) {

        return riskService.getRiskRecipients(
                authentication.getName()
        );
    }

    @GetMapping("/{recipientId}")
    public RiskCaseDetailResponse getRiskCaseDetail(
            Authentication authentication,
            @PathVariable UUID recipientId
    ) {

        return riskService.getRiskCaseDetail(
                authentication.getName(),
                recipientId
        );
    }

    @GetMapping("/{recipientId}/timeline")
    public List<RiskTimelineResponse> getRiskTimeline(
            Authentication authentication,
            @PathVariable UUID recipientId
    ) {

        return riskService.getRiskTimeline(
                authentication.getName(),
                recipientId
        );
    }

    @GetMapping("/{recipientId}/change")
    public RiskChangeResponse getRiskChange(
            Authentication authentication,
            @PathVariable UUID recipientId
    ) {

        return riskService.getRiskChange(
                authentication.getName(),
                recipientId
        );
    }
}
