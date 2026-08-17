package com.piuda.careon.consultation.service;

import com.piuda.careon.ai.domain.RiskDomain;
import com.piuda.careon.ai.service.AudioConvertService;
import com.piuda.careon.consultation.dto.*;
import com.piuda.careon.consultation.entity.Consultation;
import com.piuda.careon.consultation.entity.ConsultationStatus;
import com.piuda.careon.consultation.repository.ConsultationRepository;
import com.piuda.careon.careRecipient.entity.CareRecipient;
import com.piuda.careon.careRecipient.repository.CareRecipientRepository;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.entity.UserRole;
import com.piuda.careon.user.repository.UserRepository;
import com.piuda.careon.ai.service.SpeechToTextService;
import com.piuda.careon.ai.dto.AiAnalysisResult;
import com.piuda.careon.ai.service.AiAnalysisService;
import com.piuda.careon.ai.service.RiskScoreService;
import com.piuda.careon.notification.service.NotificationService;
import com.piuda.careon.visitSchedule.service.VisitScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final SpeechToTextService speechToTextService;
    private final AudioConvertService audioConvertService;
    private final AiAnalysisService aiAnalysisService;
    private final RiskScoreService riskScoreService;
    private final CareRecipientRepository careRecipientRepository;
    private final NotificationService notificationService;
    private final VisitScheduleService visitScheduleService;

    public ConsultationResponse createConsultation(CreateConsultationRequest request) {

        User caregiver = userRepository.findById(request.caregiverId())
                .orElseThrow(() -> new IllegalArgumentException("생활지원사를 찾을 수 없습니다."));

        Consultation consultation = Consultation.builder()
                .recipientName(request.recipientName())
                .recipientAge(request.recipientAge())
                .caregiver(caregiver)
                .consultedAt(request.consultedAt())
                .audioUrl(request.audioUrl())
                .status(ConsultationStatus.NORMAL)
                .aiTags(new ArrayList<>())
                .sttText(null)
                .aiSummary(null)
                .aiSummaryPreview(null)
                .workerFinalNote(null)
                .socialWorkerOpinion(null)
                .build();

        consultationRepository.save(consultation);

        return toResponse(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> getConsultations() {

        return consultationRepository.findAllByOrderByConsultedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ConsultationResponse toResponse(Consultation consultation) {

        return new ConsultationResponse(
                consultation.getId(),

                consultation.getRecipient() != null
                        ? consultation.getRecipient().getId()
                        : null,

                consultation.getRecipientName(),
                consultation.getRecipientAge(),

                consultation.getCaregiver() != null
                        ? consultation.getCaregiver().getId()
                        : null,

                consultation.getCaregiver() != null
                        ? consultation.getCaregiver().getName()
                        : null,

                consultation.getConsultedAt(),
                consultation.getStatus(),
                consultation.getRiskScore(),
                consultation.getEmergency(),
                consultation.getAiTags(),
                consultation.getAiSummaryPreview()
        );
    }

    @Transactional(readOnly = true)
    public ConsultationDetailResponse getConsultation(UUID id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상담일지를 찾을 수 없습니다."));

        Integer previousRiskScore = null;
        Integer riskScoreChange = null;

        if (consultation.getRecipient() != null) {

            Consultation previousConsultation =
                    consultationRepository
                            .findTopByRecipient_IdAndConsultedAtBeforeOrderByConsultedAtDesc(
                                    consultation.getRecipient().getId(),
                                    consultation.getConsultedAt()
                            )
                            .orElse(null);

            if (previousConsultation != null) {
                previousRiskScore = previousConsultation.getRiskScore();
                riskScoreChange =
                        consultation.getRiskScore() - previousRiskScore;
            }
        }

        return new ConsultationDetailResponse(
                consultation.getId(),

                consultation.getRecipient() != null
                        ? consultation.getRecipient().getId()
                        : null,

                consultation.getRecipientName(),
                consultation.getRecipientAge(),

                consultation.getCaregiver() != null
                        ? consultation.getCaregiver().getId()
                        : null,

                consultation.getCaregiver() != null
                        ? consultation.getCaregiver().getName()
                        : null,

                consultation.getConsultedAt(),
                consultation.getAudioUrl(),

                consultation.getStatus(),

                // 최종 위험도
                consultation.getRiskScore(),
                previousRiskScore,
                riskScoreChange,

                // 세부 위험도
                consultation.getCurrentRiskScore(),
                consultation.getPersistenceScore(),
                consultation.getNewChangeScore(),

                // 영역별 점수
                consultation.getNutritionScore(),
                consultation.getMentalEmotionalScore(),
                consultation.getCognitiveCommunicationScore(),
                consultation.getPhysicalFunctionalSafetyScore(),
                consultation.getSocialSupportScore(),

                consultation.getEmergency(),

                consultation.getAiTags(),
                consultation.getSttText(),
                consultation.getAiSummary(),
                consultation.getAiSummaryPreview(),
                consultation.getWorkerFinalNote(),
                consultation.getSocialWorkerOpinion()
        );
    }

    public ConsultationDetailResponse updateWorkerFinalNote(
            UUID id,
            UpdateWorkerFinalNoteRequest request
    ) {

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "상담일지를 찾을 수 없습니다."
                        )
                );

        consultation.updateWorkerFinalNote(
                request.workerFinalNote()
        );

        return getConsultation(id);
    }

    public ConsultationDetailResponse updateSocialWorkerOpinion(
            UUID id,
            UpdateSocialWorkerOpinionRequest request
    ) {

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "상담일지를 찾을 수 없습니다."
                        )
                );

        consultation.updateSocialWorkerOpinion(
                request.socialWorkerOpinion()
        );

        return getConsultation(id);
    }

    public void sendFeedback(
            String userEmail,
            UUID id
    ) {

        User sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        // 사회복지사만 전송 가능
        if (sender.getRole() != UserRole.SOCIAL_WORKER) {
            throw new IllegalArgumentException(
                    "상담 피드백을 전송할 권한이 없습니다."
            );
        }

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "상담일지를 찾을 수 없습니다."
                        )
                );


        // 같은 기관 상담인지 확인
        User caregiver = consultation.getCaregiver();

        if (caregiver == null) {
            throw new IllegalArgumentException(
                    "담당 생활지원사를 찾을 수 없습니다."
            );
        }

        if (sender.getInstitution() == null ||
                caregiver.getInstitution() == null ||
                !sender.getInstitution().getId()
                        .equals(caregiver.getInstitution().getId())) {

            throw new IllegalArgumentException(
                    "다른 기관의 상담에는 피드백을 전송할 수 없습니다."
            );
        }

        if (consultation.getSocialWorkerOpinion() == null ||
                consultation.getSocialWorkerOpinion().isBlank()) {

            throw new IllegalArgumentException(
                    "전송할 사회복지사 소견이 없습니다."
            );
        }

        UUID careRecipientId =
                consultation.getRecipient() != null
                        ? consultation.getRecipient().getId()
                        : null;

        notificationService
                .createConsultationFeedbackNotification(
                        caregiver,
                        consultation.getId(),
                        careRecipientId,
                        consultation.getRecipientName()
                );
    }

    public ConsultationDetailResponse uploadAudio(UUID id, MultipartFile file) {

        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상담일지를 찾을 수 없습니다."));

        try {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "audio");
            Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String fileName = id + "_" + originalFilename;

            Path filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());

            String audioUrl = "/uploads/audio/" + fileName;
            consultation.updateAudioUrl(audioUrl);

            return getConsultation(id);

        } catch (IOException e) {
            throw new RuntimeException("음성 파일 저장에 실패했습니다.", e);
        }
    }

    public ConsultationDetailResponse processConsultation(
            UUID caregiverId,
            UUID recipientId,
            String consultedAt,
            String languageCode,
            MultipartFile file
    ) {
        User caregiver = userRepository.findById(caregiverId)
                .orElseThrow(() -> new IllegalArgumentException("생활지원사를 찾을 수 없습니다."));

        CareRecipient recipient = careRecipientRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("대상자를 찾을 수 없습니다."));

        Path filePath = null;
        Path wavPath = null;

        try {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "temp");
            Files.createDirectories(uploadPath);

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            filePath = uploadPath.resolve(fileName);

            file.transferTo(filePath.toFile());

            wavPath = audioConvertService.convertM4aToWav(filePath);

            String sttText =
                    speechToTextService.transcribe(
                            wavPath,
                            languageCode
                    );

            AiAnalysisResult aiResult = aiAnalysisService.analyze(sttText);

            RiskScoreService.RiskCalculationResult riskResult =
                    riskScoreService.calculateRiskScore(
                            recipient.getId(),
                            aiResult.getTags()
                    );

            int riskScore = riskResult.totalScore();
            ConsultationStatus status = riskResult.status();

            Consultation consultation = Consultation.builder()
                    .caregiver(caregiver)
                    .recipient(recipient)
                    .recipientName(recipient.getName())
                    .recipientAge(recipient.getAge())
                    .consultedAt(
                            LocalDateTime.ofInstant(
                                    Instant.parse(consultedAt),
                                    ZoneId.of("Asia/Seoul")
                            )
                    )

                    .audioUrl(null)
                    .sttText(sttText)
                    .aiSummary(aiResult.getSummary())
                    .aiSummaryPreview(aiResult.getSummaryPreview())

                    .status(status)
                    .riskScore(riskScore)

                    .currentRiskScore(riskResult.currentScore())
                    .persistenceScore(riskResult.persistenceScore())
                    .newChangeScore(riskResult.newChangeScore())

                    .nutritionScore(riskResult.domainScores().getOrDefault(RiskDomain.NUTRITION, 0))
                    .mentalEmotionalScore(riskResult.domainScores().getOrDefault(RiskDomain.MENTAL_EMOTIONAL, 0))
                    .cognitiveCommunicationScore(riskResult.domainScores().getOrDefault(RiskDomain.COGNITIVE_COMMUNICATION, 0))
                    .physicalFunctionalSafetyScore(riskResult.domainScores().getOrDefault(RiskDomain.PHYSICAL_FUNCTIONAL_SAFETY, 0))
                    .socialSupportScore(riskResult.domainScores().getOrDefault(RiskDomain.SOCIAL_SUPPORT, 0))

                    .emergency(riskResult.emergency())
                    .aiTags(aiResult.getTags())
                    .workerFinalNote(null)
                    .socialWorkerOpinion(aiResult.getSocialWorkerOpinion())
                    .build();

            consultationRepository.save(consultation);

            visitScheduleService.completeScheduleForConsultation(
                    caregiver,
                    recipient,
                    consultation.getConsultedAt()
            );

            if (status == ConsultationStatus.SPECIAL_NOTE) {

                notificationService.createRiskAlertNotification(
                        caregiver,
                        consultation.getId(),
                        recipient.getId(),
                        recipient.getName(),
                        riskScore
                );

            } else {

                notificationService.createConsultationRegisteredNotification(
                        caregiver,
                        consultation.getId(),
                        recipient.getId(),
                        recipient.getName()
                );
            }

            return getConsultation(consultation.getId());

        } catch (IOException e) {
            throw new RuntimeException("상담 음성 처리에 실패했습니다.", e);

        } finally {
            try {

                if (filePath != null)
                    Files.deleteIfExists(filePath);

                if (wavPath != null)
                    Files.deleteIfExists(wavPath);

            } catch (IOException ignored) {

            }

        }
    }
}
