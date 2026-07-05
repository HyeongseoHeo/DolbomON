package com.piuda.careon.ai.service;

import com.piuda.careon.consultation.entity.Consultation;
import com.piuda.careon.consultation.entity.ConsultationStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RiskScoreService {

    private static final Map<String, Integer> TAG_SCORE_MAP = Map.ofEntries(
            Map.entry("식사감소", 15),
            Map.entry("식사거부", 25),
            Map.entry("수면문제", 10),
            Map.entry("외로움", 15),
            Map.entry("우울감", 20),
            Map.entry("불안", 20),
            Map.entry("혼동", 25),
            Map.entry("기억력저하", 25),
            Map.entry("반복발화", 20),
            Map.entry("두통", 10),
            Map.entry("어지럼증", 15),
            Map.entry("통증", 15),
            Map.entry("낙상위험", 30),
            Map.entry("거동불편", 20),
            Map.entry("복약문제", 20),
            Map.entry("위생문제", 15),
            Map.entry("체중감소", 20),
            Map.entry("사회적고립", 20),
            Map.entry("가족지원필요", 20),
            Map.entry("응급상황", 50)
    );

    private static final Set<String> HIGH_RISK_TAGS = Set.of(
            "응급상황",
            "낙상위험",
            "혼동",
            "기억력저하",
            "식사거부"
    );

    public int calculateRiskScore(List<String> currentTags, List<Consultation> recentConsultations) {
        if (currentTags == null || currentTags.isEmpty()) {
            return 0;
        }

        int score = currentTags.stream()
                .distinct()
                .mapToInt(tag -> TAG_SCORE_MAP.getOrDefault(tag, 0))
                .sum();

        score += calculateRepeatedTagBonus(currentTags, recentConsultations);
        score += calculateNewHighRiskTagBonus(currentTags, recentConsultations);

        return Math.min(score, 100);
    }

    private int calculateRepeatedTagBonus(List<String> currentTags, List<Consultation> recentConsultations) {
        if (recentConsultations == null || recentConsultations.isEmpty()) {
            return 0;
        }

        int bonus = 0;

        for (String tag : new HashSet<>(currentTags)) {
            long repeatedCount = recentConsultations.stream()
                    .filter(c -> c.getAiTags() != null && c.getAiTags().contains(tag))
                    .count();

            if (repeatedCount >= 2) {
                bonus += 15;
            } else if (repeatedCount == 1) {
                bonus += 10;
            }
        }

        return Math.min(bonus, 30);
    }

    private int calculateNewHighRiskTagBonus(List<String> currentTags, List<Consultation> recentConsultations) {
        if (recentConsultations == null || recentConsultations.isEmpty()) {
            return 0;
        }

        Set<String> previousTags = new HashSet<>();

        for (Consultation consultation : recentConsultations) {
            if (consultation.getAiTags() != null) {
                previousTags.addAll(consultation.getAiTags());
            }
        }

        boolean hasNewHighRiskTag = currentTags.stream()
                .anyMatch(tag -> HIGH_RISK_TAGS.contains(tag) && !previousTags.contains(tag));

        return hasNewHighRiskTag ? 15 : 0;
    }

    public ConsultationStatus determineStatus(int riskScore) {
        if (riskScore >= 70) {
            return ConsultationStatus.SPECIAL_NOTE;
        }

        if (riskScore >= 40) {
            return ConsultationStatus.NEED_REVIEW;
        }

        return ConsultationStatus.NORMAL;
    }
}