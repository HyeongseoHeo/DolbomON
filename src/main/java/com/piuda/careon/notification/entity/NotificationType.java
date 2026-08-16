package com.piuda.careon.notification.entity;

public enum NotificationType {

    // 사회복지사 → 생활지원사 상담 피드백
    CONSULTATION_FEEDBACK,

    // AI 위험도/이상징후
    RISK_ALERT,

    // 상담일지 등록
    CONSULTATION_REGISTERED,

    // 방문 일정 알림
    VISIT_REMINDER,

    // 특이사항
    SPECIAL_NOTE
}
