package com.piuda.careon.visitSchedule.controller;

import com.piuda.careon.visitSchedule.dto.CreateVisitScheduleRequest;
import com.piuda.careon.visitSchedule.dto.VisitScheduleResponse;
import com.piuda.careon.visitSchedule.service.VisitScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visit-schedules")
@RequiredArgsConstructor
public class VisitScheduleController {

    private final VisitScheduleService visitScheduleService;

    /**
     * 방문 일정 생성
     */
    @PostMapping
    public ResponseEntity<VisitScheduleResponse> createSchedule(
            @RequestBody CreateVisitScheduleRequest request
    ) {

        VisitScheduleResponse response =
                visitScheduleService.createSchedule(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 현재 로그인한 생활지원사의 방문 일정 조회
     */
    @GetMapping("/me")
    public ResponseEntity<List<VisitScheduleResponse>> getMySchedules(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                visitScheduleService.getMySchedules(
                        authentication.getName()
                )
        );
    }
}
