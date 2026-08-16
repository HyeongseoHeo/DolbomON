package com.piuda.careon.notification.service;

import com.piuda.careon.notification.dto.NotificationResponse;
import com.piuda.careon.notification.entity.Notification;
import com.piuda.careon.notification.entity.NotificationType;
import com.piuda.careon.notification.repository.NotificationRepository;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.entity.UserRole;
import com.piuda.careon.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자의 전체 알림 조회
     */
    public List<NotificationResponse> getMyNotifications(
            String userEmail
    ) {

        User user = findUserByEmail(userEmail);

        return notificationRepository
                .findByRecipientOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 알람 생성
     */
    @Transactional
    public void createConsultationFeedbackNotification(
            User caregiver,
            UUID consultationId,
            UUID careRecipientId,
            String recipientName
    ) {

        // 같은 상담에 대한 기존 피드백 알림이 있는지 확인
        Notification existingNotification =
                notificationRepository
                        .findByRecipientAndTypeAndConsultationId(
                                caregiver,
                                NotificationType.CONSULTATION_FEEDBACK,
                                consultationId
                        )
                        .orElse(null);

        // 이미 전송한 적이 있다면 새로 만들지 않고 재전송 처리
        if (existingNotification != null) {
            existingNotification.resend();
            return;
        }

        // 최초 전송이면 새 알림 생성
        Notification notification = Notification.builder()
                .recipient(caregiver)
                .type(NotificationType.CONSULTATION_FEEDBACK)
                .title("상담 피드백")
                .message(
                        recipientName
                                + " 어르신 상담에 대한 피드백이 도착했습니다."
                )
                .consultationId(consultationId)
                .careRecipientId(careRecipientId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 상담일지 등록 알림
     * 같은 기관의 모든 사회복지사에게 전송
     */
    @Transactional
    public void createConsultationRegisteredNotification(
            User caregiver,
            UUID consultationId,
            UUID careRecipientId,
            String recipientName
    ) {

        if (caregiver.getInstitution() == null) {
            throw new IllegalArgumentException(
                    "생활지원사의 기관 정보를 찾을 수 없습니다."
            );
        }

        List<User> socialWorkers =
                userRepository.findByInstitutionAndRole(
                        caregiver.getInstitution(),
                        UserRole.SOCIAL_WORKER
                );

        for (User socialWorker : socialWorkers) {

            Notification notification = Notification.builder()
                    .recipient(socialWorker)
                    .type(NotificationType.CONSULTATION_REGISTERED)
                    .title("상담일지 등록")
                    .message(
                            recipientName
                                    + " 어르신의 상담일지가 등록되었습니다."
                    )
                    .consultationId(consultationId)
                    .careRecipientId(careRecipientId)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        }
    }


    /**
     * 위험군 알림
     * 같은 기관의 모든 사회복지사에게 전송
     */
    @Transactional
    public void createRiskAlertNotification(
            User caregiver,
            UUID consultationId,
            UUID careRecipientId,
            String recipientName,
            int riskScore
    ) {

        if (caregiver.getInstitution() == null) {
            throw new IllegalArgumentException(
                    "생활지원사의 기관 정보를 찾을 수 없습니다."
            );
        }

        List<User> socialWorkers =
                userRepository.findByInstitutionAndRole(
                        caregiver.getInstitution(),
                        UserRole.SOCIAL_WORKER
                );

        for (User socialWorker : socialWorkers) {

            Notification notification = Notification.builder()
                    .recipient(socialWorker)
                    .type(NotificationType.RISK_ALERT)
                    .title("위험군 알림")
                    .message(
                            recipientName
                                    + " 어르신이 위험군으로 분류되었습니다. "
                                    + "위험도 "
                                    + riskScore
                                    + "점"
                    )
                    .consultationId(consultationId)
                    .careRecipientId(careRecipientId)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
        }
    }

    /**
     * 현재 로그인한 사용자의 안 읽은 알림 개수
     */
    public long getUnreadCount(
            String userEmail
    ) {

        User user = findUserByEmail(userEmail);

        return notificationRepository
                .countByRecipientAndIsReadFalse(user);
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    public void markAsRead(
            String userEmail,
            UUID notificationId
    ) {

        User user = findUserByEmail(userEmail);

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "알림을 찾을 수 없습니다."
                                )
                        );

        // 다른 사람의 알림을 읽음 처리하는 것 방지
        if (!notification.getRecipient().getId()
                .equals(user.getId())) {

            throw new IllegalArgumentException(
                    "해당 알림에 접근할 권한이 없습니다."
            );
        }

        notification.markAsRead();
    }

    private User findUserByEmail(
            String email
    ) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {

        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getConsultationId(),
                notification.getCareRecipientId(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
