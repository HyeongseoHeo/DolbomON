package com.piuda.careon.ai.domain;

import lombok.Getter;

@Getter
public enum AiTag {

    // ===== 식사 =====
    MEAL_REDUCTION("식사감소"),
    MEAL_REFUSAL("식사거부"),

    // ===== 수면 =====
    SLEEP_PROBLEM("수면문제"),

    // ===== 정신건강 =====
    LONELINESS("외로움"),
    DEPRESSION("우울감"),
    ANXIETY("불안"),

    // ===== 인지 =====
    CONFUSION("혼동"),
    MEMORY_DECLINE("기억력저하"),
    REPETITIVE_SPEECH("반복발화"),

    // ===== 신체 =====
    HEADACHE("두통"),
    DIZZINESS("어지럼증"),
    PAIN("통증"),

    // ===== 이동 =====
    FALL_RISK("낙상위험"),
    MOBILITY_DECLINE("거동불편"),

    // ===== 복약 =====
    MEDICATION_PROBLEM("복약문제"),

    // ===== 생활 =====
    HYGIENE_PROBLEM("위생문제"),
    WEIGHT_LOSS("체중감소"),
    SOCIAL_ISOLATION("사회적고립"),

    // ===== 보호 =====
    FAMILY_SUPPORT("가족지원필요"),

    // ===== 응급 =====
    EMERGENCY("응급상황");

    private final String label;

    AiTag(String label) {
        this.label = label;
    }

}
