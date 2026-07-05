package com.piuda.careon.ai.dto;

import com.piuda.careon.consultation.entity.ConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResult {

    private ConsultationStatus status;

    private List<String> tags;

    private String summary;

    private String summaryPreview;

    private List<AiChangeItem> changes;

    private String socialWorkerOpinion;
}
