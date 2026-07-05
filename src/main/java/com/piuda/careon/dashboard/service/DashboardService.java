package com.piuda.careon.dashboard.service;

import com.piuda.careon.consultation.entity.Consultation;
import com.piuda.careon.consultation.entity.ConsultationStatus;
import com.piuda.careon.consultation.repository.ConsultationRepository;
import com.piuda.careon.dashboard.dto.DashboardResponse;
import com.piuda.careon.dashboard.dto.TagStatistic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ConsultationRepository consultationRepository;

    public DashboardResponse getDashboard() {
        List<Consultation> consultations = consultationRepository.findAll();

        long total = consultations.size();

        long normalCount = consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.NORMAL)
                .count();

        long needReviewCount = consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.NEED_REVIEW)
                .count();

        long specialNoteCount = consultations.stream()
                .filter(c -> c.getStatus() == ConsultationStatus.SPECIAL_NOTE)
                .count();

        double averageRiskScore = consultations.stream()
                .map(Consultation::getRiskScore)
                .filter(score -> score != null && score > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        long highRiskCount = consultations.stream()
                .filter(c -> c.getRiskScore() != null && c.getRiskScore() >= 70)
                .count();

        Map<String, Long> tagCountMap = consultations.stream()
                .flatMap(c -> c.getAiTags().stream())
                .filter(tag ->
                        !tag.equals("AI분석실패")
                                && !tag.equals("검토필요"))
                .collect(Collectors.groupingBy(
                        tag -> tag,
                        Collectors.counting()
                ));

        long totalRecipientCount = consultations.stream()
                .map(Consultation::getRecipientName)
                .distinct()
                .count();

        Map<String, Long> tagRecipientCountMap = consultations.stream()
                .flatMap(consultation -> consultation.getAiTags().stream()
                        .filter(tag ->
                                !tag.equals("AI분석실패")
                                        && !tag.equals("검토필요"))
                        .map(tag -> consultation.getRecipientName() + "::" + tag)
                )
                .distinct()
                .map(value -> value.split("::")[1])
                .collect(Collectors.groupingBy(
                        tag -> tag,
                        Collectors.counting()
                ));

        List<TagStatistic> topTags = tagRecipientCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new TagStatistic(
                        entry.getKey(),
                        entry.getValue(),
                        totalRecipientCount == 0
                                ? 0
                                : Math.round((entry.getValue() * 1000.0 / totalRecipientCount)) / 10.0
                ))
                .toList();

        return DashboardResponse.builder()
                .totalConsultations(total)
                .normalCount(normalCount)
                .needReviewCount(needReviewCount)
                .specialNoteCount(specialNoteCount)
                .averageRiskScore(averageRiskScore)
                .highRiskCount(highRiskCount)
                .topTags(topTags)
                .build();
    }
}