package com.piuda.careon.visitSchedule.service;

import com.piuda.careon.careRecipient.entity.CareRecipient;
import com.piuda.careon.careRecipient.repository.CareRecipientRepository;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.repository.UserRepository;
import com.piuda.careon.visitSchedule.dto.CreateVisitScheduleRequest;
import com.piuda.careon.visitSchedule.dto.VisitScheduleResponse;
import com.piuda.careon.visitSchedule.entity.VisitSchedule;
import com.piuda.careon.visitSchedule.entity.VisitScheduleStatus;
import com.piuda.careon.visitSchedule.repository.VisitScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitScheduleService {

    private final VisitScheduleRepository visitScheduleRepository;
    private final UserRepository userRepository;
    private final CareRecipientRepository careRecipientRepository;

    /**
     * 방문 일정 생성
     */
    @Transactional
    public VisitScheduleResponse createSchedule(
            CreateVisitScheduleRequest request
    ) {

        User caregiver = userRepository
                .findById(request.caregiverId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "생활지원사를 찾을 수 없습니다."
                        )
                );

        CareRecipient recipient = careRecipientRepository
                .findById(request.recipientId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "대상자를 찾을 수 없습니다."
                        )
                );

        // 다른 기관의 생활지원사와 대상자를 연결하는 것 방지
        if (caregiver.getInstitution() == null
                || recipient.getInstitution() == null
                || !caregiver.getInstitution().getId()
                .equals(recipient.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "같은 기관의 생활지원사와 대상자만 방문 일정을 등록할 수 있습니다."
            );
        }

        VisitSchedule schedule = VisitSchedule.builder()
                .caregiver(caregiver)
                .recipient(recipient)
                .scheduledAt(request.scheduledAt())
                .status(VisitScheduleStatus.SCHEDULED)
                .reminderSent(false)
                .build();

        visitScheduleRepository.save(schedule);

        return toResponse(schedule);
    }

    /**
     * 현재 로그인한 생활지원사의 방문 일정 조회
     */
    public List<VisitScheduleResponse> getMySchedules(
            String userEmail
    ) {

        User caregiver = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        return visitScheduleRepository
                .findByCaregiverOrderByScheduledAtAsc(caregiver)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 방문 완료 처리
     */
    @Transactional
    public void completeScheduleForConsultation(
            User caregiver,
            CareRecipient recipient,
            LocalDateTime consultedAt
    ) {

        LocalDateTime startOfDay =
                consultedAt.toLocalDate().atStartOfDay();

        LocalDateTime endOfDay =
                consultedAt.toLocalDate()
                        .plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        visitScheduleRepository
                .findFirstByCaregiverAndRecipientAndStatusAndScheduledAtBetweenOrderByScheduledAtAsc(
                        caregiver,
                        recipient,
                        VisitScheduleStatus.SCHEDULED,
                        startOfDay,
                        endOfDay
                )
                .ifPresent(VisitSchedule::complete);
    }

    /**
     * Entity -> Response DTO
     */
    private VisitScheduleResponse toResponse(
            VisitSchedule schedule
    ) {

        return new VisitScheduleResponse(
                schedule.getId(),
                schedule.getCaregiver().getId(),
                schedule.getCaregiver().getName(),
                schedule.getRecipient().getId(),
                schedule.getRecipient().getName(),
                schedule.getScheduledAt(),
                schedule.getStatus(),
                schedule.getReminderSent(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }
}
