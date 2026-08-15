package com.piuda.careon.ai.service;

import com.piuda.careon.ai.domain.AiTag;
import com.piuda.careon.ai.domain.RiskDomain;
import com.piuda.careon.consultation.entity.Consultation;
import com.piuda.careon.consultation.entity.ConsultationStatus;
import com.piuda.careon.consultation.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RiskScoreService {
    private final ConsultationRepository consultationRepository;

    /*
     * 아래 기본점수는 MNA-SF, SGDS-K, K-MMSE,
     * K-ADL / K-IADL, WHO ICOPE 등의 평가영역을 참고하여
     * 사례관리 우선순위 판단을 위해 설계한
     * CareON 자체 위험도 가중치
     *
     * 의료 진단 점수 또는 각 척도의 공식 환산점수가 아니다.
     *
     * 현재 상담 위험도 최대점수
     *
     * 영양·섭취           14
     * 정신·정서           14
     * 인지·의사소통        14
     * 신체·기능·안전       18
     * 사회·돌봄망          10
     *
     * 현재 상담 위험도     70
     * 반복·지속성         20
     * 신규 변화           10
     *
     * 최종 Risk Score     100
     */

    private static final Map<AiTag, Integer> TAG_BASE_SCORE = new EnumMap<>(AiTag.class);

    static {

        // 영양·섭취 / 최대 14점
        // 참고: MNA-SF

        TAG_BASE_SCORE.put(
                AiTag.MEAL_REDUCTION,
                5
        );

        TAG_BASE_SCORE.put(
                AiTag.MEAL_REFUSAL,
                8
        );

        TAG_BASE_SCORE.put(
                AiTag.WEIGHT_LOSS,
                7
        );

        // 정신·정서 / 최대 14점
        // 참고: SGDS-K, WHO ICOPE

        TAG_BASE_SCORE.put(
                AiTag.SLEEP_PROBLEM,
                3
        );

        TAG_BASE_SCORE.put(
                AiTag.LONELINESS,
                4
        );

        TAG_BASE_SCORE.put(
                AiTag.ANXIETY,
                5
        );

        TAG_BASE_SCORE.put(
                AiTag.DEPRESSION,
                7
        );


        // 인지·의사소통 / 최대 14점
        // 참고: K-MMSE, WHO ICOPE

        TAG_BASE_SCORE.put(
                AiTag.REPETITIVE_SPEECH,
                4
        );

        TAG_BASE_SCORE.put(
                AiTag.MEMORY_DECLINE,
                6
        );

        TAG_BASE_SCORE.put(
                AiTag.CONFUSION,
                8
        );

        // 신체·기능·안전 / 최대 18점
        // 참고: K-ADL, K-IADL, WHO ICOPE

        TAG_BASE_SCORE.put(
                AiTag.HEADACHE,
                2
        );

        TAG_BASE_SCORE.put(
                AiTag.PAIN,
                3
        );

        TAG_BASE_SCORE.put(
                AiTag.DIZZINESS,
                4
        );

        TAG_BASE_SCORE.put(
                AiTag.HYGIENE_PROBLEM,
                4
        );

        TAG_BASE_SCORE.put(
                AiTag.MOBILITY_DECLINE,
                6
        );

        TAG_BASE_SCORE.put(
                AiTag.MEDICATION_PROBLEM,
                6
        );

        TAG_BASE_SCORE.put(
                AiTag.FALL_RISK,
                8
        );

        // 사회·돌봄망 / 최대 10점
        // 참고: WHO ICOPE Social / Caregiver Support

        TAG_BASE_SCORE.put(
                AiTag.FAMILY_SUPPORT,
                4
        );

        TAG_BASE_SCORE.put(
                AiTag.SOCIAL_ISOLATION,
                7
        );

        /*
         * EMERGENCY는 일반 점수 합산 대상이 아니다.
         * 최종 RiskScore 계산 단계에서
         * 즉시 100점 + SPECIAL_NOTE 처리할 예정.
         */
    }


    /**
     * 현재 상담 위험도 계산
     *
     * 최대 70점.
     *
     * 태그별 기본점수를 위험영역별로 합산한 뒤
     * 각 영역의 최대점수를 적용한다.
     */
    public CurrentRiskResult calculateCurrentRisk(
            List<String> currentTagLabels
    ) {

        if (currentTagLabels == null ||
                currentTagLabels.isEmpty()) {

            return emptyCurrentRiskResult();
        }

        // 1. 문자열 태그 → AiTag 변환

        Set<AiTag> currentTags = new HashSet<>();

        for (String label : currentTagLabels) {

            try {

                currentTags.add(
                        AiTag.fromLabel(label)
                );

            } catch (IllegalArgumentException ignored) {

                /*
                 * 예:
                 * AI분석실패
                 * 검토필요
                 *
                 * 표준 AiTag가 아닌 시스템 태그는
                 * RiskScore 계산에서 제외
                 */
            }
        }

        // 2. 응급상황 여부

        boolean emergency =
                currentTags.contains(
                        AiTag.EMERGENCY
                );

        // 3. 영역별 Raw Score 초기화

        Map<RiskDomain, Integer> rawDomainScores =
                createEmptyDomainScoreMap();

        // 4. 태그 기본점수 합산

        for (AiTag tag : currentTags) {

            /*
             * 응급상황은 별도 처리.
             */
            if (tag == AiTag.EMERGENCY) {
                continue;
            }

            int baseScore =
                    TAG_BASE_SCORE.getOrDefault(
                            tag,
                            0
                    );

            RiskDomain domain =
                    tag.getDomain();

            rawDomainScores.merge(
                    domain,
                    baseScore,
                    Integer::sum
            );
        }


        // 5. 영역별 최대점수 적용

        Map<RiskDomain, Integer> finalDomainScores =
                new EnumMap<>(
                        RiskDomain.class
                );

        for (
                Map.Entry<RiskDomain, Integer> entry
                : rawDomainScores.entrySet()
        ) {

            RiskDomain domain =
                    entry.getKey();

            int rawScore =
                    entry.getValue();

            int cappedScore =
                    Math.min(
                            rawScore,
                            domain.getMaxScore()
                    );

            finalDomainScores.put(
                    domain,
                    cappedScore
            );
        }


        // 6. 현재 상담 총 위험도

        int currentScore =
                finalDomainScores
                        .values()
                        .stream()
                        .mapToInt(
                                Integer::intValue
                        )
                        .sum();


        return new CurrentRiskResult(
                currentScore,
                finalDomainScores,
                emergency
        );
    }

    /**
     * 반복·지속성 점수 계산
     * 해당 대상자의 최근 이전 상담 3건에서
     * 얼마나 반복되었는지 계산
     *
     * 최근 3건 중
     * 1건 반복 → +3
     * 2건 반복 → +5
     * 3건 반복 → +7
     *
     * 전체 최대 20점
     */
    public int calculatePersistenceScore(
            UUID recipientId,
            List<String> currentTagLabels
    ) {

        if (recipientId == null ||
                currentTagLabels == null ||
                currentTagLabels.isEmpty()) {
            return 0;
        }

        // 해당 대상자의 최근 상담 3건 조회
        List<Consultation> recentConsultations =
                consultationRepository
                        .findTop3ByRecipient_IdOrderByConsultedAtDesc(
                                recipientId
                        );

        /*
         * 비교할 과거 상담이 없으면
         * 반복·지속성 점수를 계산할 수 없으므로 0점 처리한다.
         */

        if (recentConsultations.isEmpty()) {
            return 0;
        }

        // 현재 상담 태그 변환
        Set<AiTag> currentTags = new HashSet<>();

        for (String label : currentTagLabels) {

            try {

                AiTag tag = AiTag.fromLabel(label);

                // 응급상황은 반복점수 계산에서 제외
                if (tag != AiTag.EMERGENCY) {
                    currentTags.add(tag);
                }

            } catch (IllegalArgumentException ignored) {
                // 표준 AiTag가 아닌 값은 제외
            }
        }

        int persistenceScore = 0;

        // 현재 태그 각각에 대해 최근 3건에서 반복 횟수 확인
        for (AiTag currentTag : currentTags) {

            long repeatedCount =
                    recentConsultations.stream()
                            .filter(consultation ->
                                    consultation.getAiTags() != null
                                            && consultation.getAiTags()
                                            .contains(currentTag.getLabel())
                            )
                            .count();

            if (repeatedCount >= 3) {

                persistenceScore += 7;

            } else if (repeatedCount == 2) {

                persistenceScore += 5;

            } else if (repeatedCount == 1) {

                persistenceScore += 3;
            }
        }

        // 반복·지속성 영역 최대 20점
        return Math.min(
                persistenceScore,
                20
        );
    }

    /**
     * 신규 변화 점수 계산
     *
     * 현재 상담에서 감지된 태그 중
     * 해당 대상자의 최근 이전 상담 3건에 존재하지 않았던
     * 새로운 태그를 찾아 점수를 부여한다.
     *
     * 태그 기본점수 기준:
     *
     * 2~3점  → +1
     * 4~5점  → +2
     * 6점 이상 → +4
     *
     * 전체 최대 10점.
     *
     * ※ EMERGENCY는 신규 변화 점수에 포함하지 않는다.
     *    최종 RiskScore 계산에서 즉시 100점으로 별도 처리한다.
     */
    public int calculateNewChangeScore(
            UUID recipientId,
            List<String> currentTagLabels
    ) {

        if (recipientId == null ||
                currentTagLabels == null ||
                currentTagLabels.isEmpty()) {

            return 0;
        }

        // 1. 해당 대상자의 최근 이전 상담 3건 조회

        List<Consultation> recentConsultations =
                consultationRepository
                        .findTop3ByRecipient_IdOrderByConsultedAtDesc(
                                recipientId
                        );

        /*
         * 비교할 과거 데이터가 없기 때문에
         * 현재 태그를 "신규 변화"로 판단하지 않는다.
         */

        if (recentConsultations.isEmpty()) {
            return 0;
        }

        // 2. 현재 상담 태그 → AiTag 변환

        Set<AiTag> currentTags = new HashSet<>();

        for (String label : currentTagLabels) {

            try {

                AiTag tag = AiTag.fromLabel(label);

                // 응급상황은 신규 변화 점수에서 제외
                if (tag != AiTag.EMERGENCY) {
                    currentTags.add(tag);
                }

            } catch (IllegalArgumentException ignored) {

                // 표준 AiTag가 아닌 시스템 태그 제외
            }
        }

        // 3. 최근 3건에 등장했던 모든 태그 수집

        Set<AiTag> previousTags = new HashSet<>();

        for (Consultation consultation : recentConsultations) {

            if (consultation.getAiTags() == null) {
                continue;
            }

            for (String previousLabel : consultation.getAiTags()) {

                try {

                    AiTag previousTag =
                            AiTag.fromLabel(previousLabel);

                    if (previousTag != AiTag.EMERGENCY) {
                        previousTags.add(previousTag);
                    }

                } catch (IllegalArgumentException ignored) {

                    // 비표준 태그 제외
                }
            }
        }

        // 4. 신규 태그 확인 및 점수 계산

        int newChangeScore = 0;

        for (AiTag currentTag : currentTags) {

            /*
             * 최근 3건 중 한 번이라도 존재했다면
             * 신규 태그가 아니므로 점수를 주지 않는다.
             */
            if (previousTags.contains(currentTag)) {
                continue;
            }

            int baseScore =
                    TAG_BASE_SCORE.getOrDefault(
                            currentTag,
                            0
                    );

            /*
             * 기존 태그 기본점수를 이용해서
             * 신규 변화 가중치를 결정한다.
             */
            if (baseScore >= 6) {

                newChangeScore += 4;

            } else if (baseScore >= 4) {

                newChangeScore += 2;

            } else if (baseScore >= 2) {

                newChangeScore += 1;
            }
        }

        // 5. 신규 변화 최대 10점

        return Math.min(
                newChangeScore,
                10
        );
    }

    /**
     * 위험 태그가 없을 때
     * 0점 결과 생성.
     */
    private CurrentRiskResult emptyCurrentRiskResult() {

        return new CurrentRiskResult(
                0,
                createEmptyDomainScoreMap(),
                false
        );
    }


    /**
     * 영역별 점수 Map 생성.
     */
    private Map<RiskDomain, Integer>
    createEmptyDomainScoreMap() {

        Map<RiskDomain, Integer> scores =
                new EnumMap<>(
                        RiskDomain.class
                );

        scores.put(
                RiskDomain.NUTRITION,
                0
        );

        scores.put(
                RiskDomain.MENTAL_EMOTIONAL,
                0
        );

        scores.put(
                RiskDomain.COGNITIVE_COMMUNICATION,
                0
        );

        scores.put(
                RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY,
                0
        );

        scores.put(
                RiskDomain.SOCIAL_SUPPORT,
                0
        );

        return scores;
    }

    /**
     * 최종 CareON Risk Score 계산
     *
     * 현재 상담 위험도 0~70
     * + 반복·지속성 0~20
     * + 신규 변화 0~10
     *
     * = 최대 100점
     *
     * 단, 응급상황 태그가 포함된 경우
     * 일반 계산 결과와 관계없이 100점 + SPECIAL_NOTE 처리
     */
    public RiskCalculationResult calculateRiskScore(
            UUID recipientId,
            List<String> currentTagLabels
    ) {

        // 1. 현재 상담 위험도 계산
        CurrentRiskResult currentRisk =
                calculateCurrentRisk(currentTagLabels);

        // 2. 응급상황 Override
        if (currentRisk.emergency()) {

            return new RiskCalculationResult(
                    100,
                    ConsultationStatus.SPECIAL_NOTE,
                    currentRisk.score(),
                    0,
                    0,
                    currentRisk.domainScores(),
                    true
            );
        }

        // 3. 반복·지속성 점수
        int persistenceScore =
                calculatePersistenceScore(
                        recipientId,
                        currentTagLabels
                );

        // 4. 신규 변화 점수
        int newChangeScore =
                calculateNewChangeScore(
                        recipientId,
                        currentTagLabels
                );

        // 5. 최종 합산
        int totalScore =
                currentRisk.score()
                        + persistenceScore
                        + newChangeScore;

        // 안전하게 최대 100점 제한
        totalScore = Math.min(
                totalScore,
                100
        );

        // 6. 상태 결정
        ConsultationStatus status =
                determineStatus(totalScore);

        // 7. 세부 계산 결과까지 반환
        return new RiskCalculationResult(
                totalScore,
                status,
                currentRisk.score(),
                persistenceScore,
                newChangeScore,
                currentRisk.domainScores(),
                false
        );
    }

    /**
     * 0 ~ 39
     * NORMAL
     *
     * 40 ~ 69
     * NEED_REVIEW
     *
     * 70 ~ 100
     * SPECIAL_NOTE
     */

    public ConsultationStatus determineStatus(
            int riskScore
    ) {

        if (riskScore >= 70) {

            return ConsultationStatus.SPECIAL_NOTE;
        }

        if (riskScore >= 40) {

            return ConsultationStatus.NEED_REVIEW;
        }

        return ConsultationStatus.NORMAL;
    }

    /**
     * 현재 상담 위험도 계산 결과
     * score
     * → 현재 상담 점수 (0~70)
     *
     * domainScores
     * → 영역별 세부 점수
     *
     * emergency
     * → 응급상황 태그 감지 여부
     */
    public record CurrentRiskResult(

            int score,

            Map<RiskDomain, Integer> domainScores,

            boolean emergency

    ) {
    }

    /**
     * 최종 위험도 계산 결과
     *
     * totalScore
     * → 최종 Risk Score (0~100)
     *
     * status
     * → NORMAL / NEED_REVIEW / SPECIAL_NOTE
     *
     * currentScore
     * → 현재 상담 위험도 (0~70)
     *
     * persistenceScore
     * → 반복·지속성 (0~20)
     *
     * newChangeScore
     * → 신규 변화 (0~10)
     *
     * domainScores
     * → 영역별 세부 점수
     *
     * emergency
     * → 응급상황 감지 여부
     */
    public record RiskCalculationResult(

            int totalScore,

            ConsultationStatus status,

            int currentScore,

            int persistenceScore,

            int newChangeScore,

            Map<RiskDomain, Integer> domainScores,

            boolean emergency

    ) {
    }
}
