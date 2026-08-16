package com.piuda.careon.visitSchedule.repository;

import com.piuda.careon.user.entity.User;
import com.piuda.careon.visitSchedule.entity.VisitSchedule;
import com.piuda.careon.visitSchedule.entity.VisitScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VisitScheduleRepository
        extends JpaRepository<VisitSchedule, UUID> {

    // 특정 생활지원사의 방문 일정 - 시간순
    List<VisitSchedule> findByCaregiverOrderByScheduledAtAsc(
            User caregiver
    );

    // 알림 대상 방문 일정 조회
    List<VisitSchedule>
    findByStatusAndReminderSentFalseAndScheduledAtBetween(
            VisitScheduleStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
}
