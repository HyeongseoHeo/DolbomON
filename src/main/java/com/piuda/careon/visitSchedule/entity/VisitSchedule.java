package com.piuda.careon.visitSchedule.entity;

import com.piuda.careon.careRecipient.entity.CareRecipient;
import com.piuda.careon.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visit_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VisitSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 방문 담당 생활지원사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    // 방문 대상자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private CareRecipient recipient;

    // 방문 예정 시간
    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VisitScheduleStatus status;

    // 30분 전 알림 전송 여부
    @Column(nullable = false)
    private Boolean reminderSent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        if (this.status == null) {
            this.status = VisitScheduleStatus.SCHEDULED;
        }

        if (this.reminderSent == null) {
            this.reminderSent = false;
        }

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void markReminderSent() {
        this.reminderSent = true;
    }

    public void complete() {
        this.status = VisitScheduleStatus.COMPLETED;
    }

    public void cancel() {
        this.status = VisitScheduleStatus.CANCELED;
    }
}
