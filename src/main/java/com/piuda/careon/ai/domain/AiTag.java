package com.piuda.careon.ai.domain;

public enum AiTag {

    // ===== 영양·섭취 =====
    MEAL_REDUCTION(
            "식사감소",
            RiskDomain.NUTRITION
    ),

    MEAL_REFUSAL(
            "식사거부",
            RiskDomain.NUTRITION
    ),

    WEIGHT_LOSS(
            "체중감소",
            RiskDomain.NUTRITION
    ),

    // ===== 정신·정서 =====
    SLEEP_PROBLEM(
            "수면문제",
            RiskDomain.MENTAL_EMOTIONAL
    ),

    LONELINESS(
            "외로움",
            RiskDomain.MENTAL_EMOTIONAL
    ),

    ANXIETY(
            "불안",
            RiskDomain.MENTAL_EMOTIONAL
    ),

    DEPRESSION(
            "우울감",
            RiskDomain.MENTAL_EMOTIONAL
    ),

    // ===== 인지·의사소통 =====
    REPETITIVE_SPEECH(
            "반복발화",
            RiskDomain.COGNITIVE_COMMUNICATION
    ),

    MEMORY_DECLINE(
            "기억력저하",
            RiskDomain.COGNITIVE_COMMUNICATION
    ),

    CONFUSION(
            "혼동",
            RiskDomain.COGNITIVE_COMMUNICATION
    ),

    // ===== 신체·기능·안전 =====
    HEADACHE(
            "두통",
            RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY
    ),

    PAIN(
            "통증",
            RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY
    ),

    DIZZINESS(
            "어지럼증",
            RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY
    ),

    HYGIENE_PROBLEM(
            "위생문제",
            RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY
    ),

    MOBILITY_DECLINE(
            "거동불편",
            RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY
    ),

    MEDICATION_PROBLEM(
            "복약문제",
            RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY
    ),

    FALL_RISK(
            "낙상위험",
            RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY
    ),

    // ===== 사회·돌봄망 =====
    FAMILY_SUPPORT(
            "가족지원필요",
            RiskDomain.SOCIAL_SUPPORT
    ),

    SOCIAL_ISOLATION(
            "사회적고립",
            RiskDomain.SOCIAL_SUPPORT
    ),

    // ===== 응급 =====
    EMERGENCY(
            "응급상황",
            RiskDomain.EMERGENCY
    );

    private final String label;
    private final RiskDomain domain;

    AiTag(
            String label,
            RiskDomain domain
    ) {
        this.label = label;
        this.domain = domain;
    }

    public String getLabel() {
        return label;
    }

    public RiskDomain getDomain() {
        return domain;
    }

    public static AiTag fromLabel(String label) {
        for (AiTag tag : values()) {
            if (tag.getLabel().equals(label)) {
                return tag;
            }
        }

        throw new IllegalArgumentException(
                "지원하지 않는 AI 태그입니다: " + label
        );
    }
}
