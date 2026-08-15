package com.piuda.careon.ai.domain;

public enum RiskDomain {

    NUTRITION("영양·섭취", 14),
    MENTAL_EMOTIONAL("정신·정서", 14),
    COGNITIVE_COMMUNICATION("인지·의사소통", 14),
    PHYSICAL_FUNCTIONAL_SAFETY("신체·기능·안전", 18),
    SOCIAL_SUPPORT("사회·돌봄망", 10),

    // 별도 처리
    EMERGENCY("응급", 0);

    private final String label;
    private final int maxScore;

    RiskDomain(String label, int maxScore) {
        this.label = label;
        this.maxScore = maxScore;
    }

    public String getLabel() {
        return label;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
