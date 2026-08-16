package com.piuda.careon.notification.repository;

import com.piuda.careon.notification.entity.Notification;
import com.piuda.careon.notification.entity.NotificationType;
import com.piuda.careon.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    // 특정 사용자의 전체 알림 - 최신순
    List<Notification> findByRecipientOrderByCreatedAtDesc(
            User recipient
    );

    // 특정 사용자의 읽지 않은 알림 - 최신순
    Optional<Notification> findByRecipientAndTypeAndConsultationId(
            User recipient,
            NotificationType type,
            UUID consultationId
    );

    // 안 읽은 알림 개수
    long countByRecipientAndIsReadFalse(
            User recipient
    );
}
