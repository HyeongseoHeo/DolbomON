package com.piuda.careon.risk.service;

import com.piuda.careon.careRecipient.entity.CareRecipient;
import com.piuda.careon.careRecipient.repository.CareRecipientRepository;
import com.piuda.careon.consultation.entity.Consultation;
import com.piuda.careon.consultation.entity.ConsultationStatus;
import com.piuda.careon.consultation.repository.ConsultationRepository;
import com.piuda.careon.risk.dto.RiskCaseDetailResponse;
import com.piuda.careon.risk.dto.RiskRecipientResponse;
import com.piuda.careon.risk.dto.RiskTimelineResponse;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.repository.UserRepository;
import com.piuda.careon.risk.dto.RiskChangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiskService {

    private final UserRepository userRepository;
    private final CareRecipientRepository careRecipientRepository;
    private final ConsultationRepository consultationRepository;

    public List<RiskRecipientResponse> getRiskRecipients(
            String userEmail
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        if (currentUser.getInstitution() == null) {
            throw new IllegalArgumentException(
                    "소속 기관 정보를 찾을 수 없습니다."
            );
        }

        List<CareRecipient> recipients =
                careRecipientRepository
                        .findByInstitutionOrderByCreatedAtDesc(
                                currentUser.getInstitution()
                        );

        return recipients.stream()
                .map(this::toRiskResponse)
                .sorted(
                        Comparator
                                .comparingInt(
                                        (RiskRecipientResponse r) ->
                                                statusPriority(r.status())
                                )
                                .thenComparing(
                                        RiskRecipientResponse::riskScore,
                                        Comparator.reverseOrder()
                                )
                )
                .toList();
    }

    private RiskRecipientResponse toRiskResponse(
            CareRecipient recipient
    ) {

        Consultation latestConsultation =
                consultationRepository
                        .findTopByRecipient_IdOrderByConsultedAtDesc(
                                recipient.getId()
                        )
                        .orElse(null);

        if (latestConsultation == null) {

            return new RiskRecipientResponse(
                    recipient.getId(),
                    recipient.getName(),
                    recipient.getAge(),

                    recipient.getCaregiver() != null
                            ? recipient.getCaregiver().getId()
                            : null,

                    recipient.getCaregiver() != null
                            ? recipient.getCaregiver().getName()
                            : null,

                    List.of(),

                    0,
                    ConsultationStatus.NORMAL,
                    false,

                    null
            );
        }

        return new RiskRecipientResponse(
                recipient.getId(),
                recipient.getName(),
                recipient.getAge(),

                recipient.getCaregiver() != null
                        ? recipient.getCaregiver().getId()
                        : null,

                recipient.getCaregiver() != null
                        ? recipient.getCaregiver().getName()
                        : null,

                latestConsultation.getAiTags(),

                latestConsultation.getRiskScore(),
                latestConsultation.getStatus(),
                latestConsultation.getEmergency(),

                latestConsultation.getConsultedAt()
        );
    }

    public RiskCaseDetailResponse getRiskCaseDetail(
            String userEmail,
            UUID recipientId
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        CareRecipient recipient =
                careRecipientRepository.findById(recipientId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "대상자를 찾을 수 없습니다."
                                )
                        );

        // 같은 기관 대상자인지 확인
        if (currentUser.getInstitution() == null ||
                recipient.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(recipient.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "해당 대상자를 조회할 권한이 없습니다."
            );
        }

        Consultation latestConsultation =
                consultationRepository
                        .findTopByRecipient_IdOrderByConsultedAtDesc(
                                recipientId
                        )
                        .orElse(null);

        if (latestConsultation == null) {

            return new RiskCaseDetailResponse(
                    recipient.getId(),
                    recipient.getName(),
                    recipient.getAge(),

                    recipient.getCaregiver() != null
                            ? recipient.getCaregiver().getId()
                            : null,

                    recipient.getCaregiver() != null
                            ? recipient.getCaregiver().getName()
                            : null,

                    0,
                    ConsultationStatus.NORMAL,

                    0,
                    0,
                    0,

                    0,
                    0,
                    0,
                    0,
                    0,

                    false,

                    List.of(),

                    null,

                    null,
                    null
            );
        }

        return new RiskCaseDetailResponse(
                recipient.getId(),
                recipient.getName(),
                recipient.getAge(),

                recipient.getCaregiver() != null
                        ? recipient.getCaregiver().getId()
                        : null,

                recipient.getCaregiver() != null
                        ? recipient.getCaregiver().getName()
                        : null,

                latestConsultation.getRiskScore(),
                latestConsultation.getStatus(),

                latestConsultation.getCurrentRiskScore(),
                latestConsultation.getPersistenceScore(),
                latestConsultation.getNewChangeScore(),

                latestConsultation.getNutritionScore(),
                latestConsultation.getMentalEmotionalScore(),
                latestConsultation.getCognitiveCommunicationScore(),
                latestConsultation.getPhysicalFunctionalSafetyScore(),
                latestConsultation.getSocialSupportScore(),

                latestConsultation.getEmergency(),

                latestConsultation.getAiTags(),

                latestConsultation.getConsultedAt(),

                latestConsultation.getAiSummary(),
                latestConsultation.getSocialWorkerOpinion()
        );
    }

    public RiskChangeResponse getRiskChange(
            String userEmail,
            UUID recipientId
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        CareRecipient recipient =
                careRecipientRepository.findById(recipientId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "대상자를 찾을 수 없습니다."
                                )
                        );

        // 다른 기관 대상자 조회 방지
        if (currentUser.getInstitution() == null ||
                recipient.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(recipient.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "해당 대상자를 조회할 권한이 없습니다."
            );
        }

        List<Consultation> consultations =
                consultationRepository
                        .findTop2ByRecipient_IdOrderByConsultedAtDesc(
                                recipientId
                        );

        // 상담 자체가 없는 경우
        if (consultations.isEmpty()) {

            return new RiskChangeResponse(
                    recipientId,
                    0,
                    null,
                    0,
                    "NO_PREVIOUS_DATA",
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    null
            );
        }

        Consultation current = consultations.get(0);

        // 첫 상담인 경우
        if (consultations.size() == 1) {

            return new RiskChangeResponse(
                    recipientId,
                    current.getRiskScore(),
                    null,
                    0,
                    "NO_PREVIOUS_DATA",
                    List.of(),
                    List.of(),
                    List.of(),
                    current.getConsultedAt(),
                    null
            );
        }

        Consultation previous = consultations.get(1);

        int scoreChange =
                current.getRiskScore()
                        - previous.getRiskScore();

        String trend;

        if (scoreChange > 0) {
            trend = "WORSENED";
        } else if (scoreChange < 0) {
            trend = "IMPROVED";
        } else {
            trend = "UNCHANGED";
        }

        Set<String> currentTags =
                new HashSet<>(
                        current.getAiTags() != null
                                ? current.getAiTags()
                                : List.of()
                );

        Set<String> previousTags =
                new HashSet<>(
                        previous.getAiTags() != null
                                ? previous.getAiTags()
                                : List.of()
                );

        // 이번 상담에 새로 등장
        Set<String> newTags =
                new HashSet<>(currentTags);

        newTags.removeAll(previousTags);


        // 이전에는 있었는데 이번에는 사라짐
        Set<String> resolvedTags =
                new HashSet<>(previousTags);

        resolvedTags.removeAll(currentTags);


        // 두 상담 모두 존재
        Set<String> persistentTags =
                new HashSet<>(currentTags);

        persistentTags.retainAll(previousTags);


        return new RiskChangeResponse(
                recipientId,

                current.getRiskScore(),
                previous.getRiskScore(),
                scoreChange,

                trend,

                new ArrayList<>(newTags),
                new ArrayList<>(resolvedTags),
                new ArrayList<>(persistentTags),

                current.getConsultedAt(),
                previous.getConsultedAt()
        );
    }

    public List<RiskTimelineResponse> getRiskTimeline(
            String userEmail,
            UUID recipientId
    ) {

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        CareRecipient recipient =
                careRecipientRepository.findById(recipientId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "대상자를 찾을 수 없습니다."
                                )
                        );

        // 다른 기관 대상자 조회 방지
        if (currentUser.getInstitution() == null ||
                recipient.getInstitution() == null ||
                !currentUser.getInstitution().getId()
                        .equals(recipient.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "해당 대상자를 조회할 권한이 없습니다."
            );
        }

        return consultationRepository
                .findByRecipient_IdOrderByConsultedAtDesc(recipientId)
                .stream()
                .map(consultation ->
                        new RiskTimelineResponse(
                                consultation.getId(),
                                consultation.getConsultedAt(),

                                consultation.getRiskScore(),
                                consultation.getStatus(),

                                consultation.getCurrentRiskScore(),
                                consultation.getPersistenceScore(),
                                consultation.getNewChangeScore(),

                                consultation.getAiTags(),

                                consultation.getEmergency()
                        )
                )
                .toList();
    }

    private int statusPriority(ConsultationStatus status) {

        if (status == ConsultationStatus.SPECIAL_NOTE) {
            return 0;
        }

        if (status == ConsultationStatus.NEED_REVIEW) {
            return 1;
        }

        return 2;
    }
}