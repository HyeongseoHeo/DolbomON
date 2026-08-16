package com.piuda.careon.visitSchedule.service;

import com.piuda.careon.notification.service.NotificationService;
import com.piuda.careon.visitSchedule.entity.VisitSchedule;
import com.piuda.careon.visitSchedule.entity.VisitScheduleStatus;
import com.piuda.careon.visitSchedule.repository.VisitScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VisitReminderScheduler {

    private final VisitScheduleRepository visitScheduleRepository;
    private final NotificationService notificationService;

    /**
     * 1분마다 방문 예정 일정 확인
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void createVisitReminders() {

        LocalDateTime now = LocalDateTime.now();

        // 지금부터 30분 이내에 예정된 방문 중
        // 아직 알림을 보내지 않은 일정 조회
        LocalDateTime reminderLimit = now.plusMinutes(30);

        List<VisitSchedule> schedules =
                visitScheduleRepository
                        .findByStatusAndReminderSentFalseAndScheduledAtBetween(
                                VisitScheduleStatus.SCHEDULED,
                                now,
                                reminderLimit
                        );

        for (VisitSchedule schedule : schedules) {

            notificationService.createVisitReminderNotification(
                    schedule.getCaregiver(),
                    schedule.getId(),
                    schedule.getRecipient().getId(),
                    schedule.getRecipient().getName(),
                    schedule.getScheduledAt()
            );

            schedule.markReminderSent();
        }
    }
}
