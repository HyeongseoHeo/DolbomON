package com.piuda.careon.consultation.entity;

import com.piuda.careon.user.entity.User;
import com.piuda.careon.careRecipient.entity.CareRecipient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consultations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String recipientName;

    private Integer recipientAge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private CareRecipient recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    @Column(nullable = false)
    private LocalDateTime consultedAt;

    @Column(length = 500)
    private String audioUrl;

    @ElementCollection
    @CollectionTable(
            name = "consultation_tags",
            joinColumns = @JoinColumn(name = "consultation_id")
    )
    @Column(name = "tag", length = 50)
    private List<String> aiTags;

    @Column(columnDefinition = "TEXT")
    private String sttText;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(length = 255)
    private String aiSummaryPreview;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsultationStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Integer riskScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentRiskScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer persistenceScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer newChangeScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer nutritionScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer mentalEmotionalScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer cognitiveCommunicationScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer physicalFunctionalSafetyScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer socialSupportScore = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emergency = false;

    @Column(columnDefinition = "TEXT")
    private String workerFinalNote;

    @Column(columnDefinition = "TEXT")
    private String socialWorkerOpinion;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = ConsultationStatus.NORMAL;
        }
        if (this.aiTags == null) {
            this.aiTags = new ArrayList<>();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    public void updateAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public void updateWorkerFinalNote(String workerFinalNote) {this.workerFinalNote = workerFinalNote;}
    public void updateSocialWorkerOpinion(String socialWorkerOpinion) {this.socialWorkerOpinion = socialWorkerOpinion;}

    public void updateAiResult(
            String sttText,
            String aiSummary,
            String aiSummaryPreview,
            ConsultationStatus status,
            Integer riskScore,
            Integer currentRiskScore,
            Integer persistenceScore,
            Integer newChangeScore,
            Integer nutritionScore,
            Integer mentalEmotionalScore,
            Integer cognitiveCommunicationScore,
            Integer physicalFunctionalSafetyScore,
            Integer socialSupportScore,
            Boolean emergency,
            List<String> aiTags
    ) {
        this.sttText = sttText;
        this.aiSummary = aiSummary;
        this.aiSummaryPreview = aiSummaryPreview;
        this.status = status;

        this.riskScore = riskScore;
        this.currentRiskScore = currentRiskScore;
        this.persistenceScore = persistenceScore;
        this.newChangeScore = newChangeScore;

        this.nutritionScore = nutritionScore;
        this.mentalEmotionalScore = mentalEmotionalScore;
        this.cognitiveCommunicationScore = cognitiveCommunicationScore;
        this.physicalFunctionalSafetyScore = physicalFunctionalSafetyScore;
        this.socialSupportScore = socialSupportScore;

        this.emergency = emergency;

        this.aiTags = aiTags;

        // STT 완료 후 원본 음성은 저장 X
        this.audioUrl = null;
    }
}
