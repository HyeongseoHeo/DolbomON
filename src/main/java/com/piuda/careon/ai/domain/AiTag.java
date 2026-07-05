package com.piuda.careon.ai.domain;

public enum AiTag {

    // ===== 식사 =====
    MEAL_REDUCTION("식사감소", 15),
    MEAL_REFUSAL("식사거부", 25),

    // ===== 수면 =====
    SLEEP_PROBLEM("수면문제", 10),

    // ===== 정신건강 =====
    LONELINESS("외로움", 15),
    DEPRESSION("우울감", 20),
    ANXIETY("불안", 20),
    CONFUSION("혼동", 25),

    // ===== 인지 =====
    MEMORY_DECLINE("기억력저하", 25),
    REPETITIVE_SPEECH("반복발화", 20),

    // ===== 신체 =====
    HEADACHE("두통", 10),
    DIZZINESS("어지럼증", 15),
    PAIN("통증", 15),

    // ===== 이동 =====
    FALL_RISK("낙상위험", 30),
    MOBILITY_DECLINE("거동불편", 20),

    // ===== 복약 =====
    MEDICATION_PROBLEM("복약문제", 20),

    // ===== 생활 =====
    HYGIENE_PROBLEM("위생문제", 15),
    WEIGHT_LOSS("체중감소", 20),
    SOCIAL_ISOLATION("사회적고립", 20),

    // ===== 보호 =====
    FAMILY_SUPPORT("가족지원필요", 20),

    // ===== 응급 =====
    EMERGENCY("응급상황", 50);

    private final String label;
    private final int score;

    AiTag(String label, int score) {
        this.label = label;
        this.score = score;
    }

    public String getLabel() {
        return label;
    }

    public int getScore() {
        return score;
    }
}
