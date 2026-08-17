package com.piuda.careon.statistics.service;

import com.piuda.careon.consultation.entity.Consultation;
import com.piuda.careon.consultation.entity.ConsultationStatus;
import com.piuda.careon.consultation.repository.ConsultationRepository;
import com.piuda.careon.statistics.domain.StatisticsPeriod;
import com.piuda.careon.statistics.dto.AbnormalTrendItem;
import com.piuda.careon.statistics.dto.StatisticsDateRange;
import com.piuda.careon.statistics.dto.StatisticsSummaryResponse;
import com.piuda.careon.user.entity.User;
import com.piuda.careon.user.repository.UserRepository;
import com.piuda.careon.visitSchedule.entity.VisitSchedule;
import com.piuda.careon.visitSchedule.entity.VisitScheduleStatus;
import com.piuda.careon.visitSchedule.repository.VisitScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final ConsultationRepository consultationRepository;
    private final VisitScheduleRepository visitScheduleRepository;
    private final StatisticsPeriodResolver periodResolver;
    private final UserRepository userRepository;

    public StatisticsSummaryResponse getSummary(
            String userEmail,
            StatisticsPeriod period,
            LocalDate customStartDate,
            LocalDate customEndDate
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자 정보를 찾을 수 없습니다."
                        )
                );

        if (user.getInstitution() == null) {
            throw new IllegalArgumentException(
                    "기관 정보를 찾을 수 없습니다."
            );
        }

        StatisticsDateRange range =
                periodResolver.resolve(
                        period,
                        customStartDate,
                        customEndDate
                );

        LocalDateTime start =
                range.startDate().atStartOfDay();

        LocalDateTime end =
                range.endDate()
                        .plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        List<Consultation> consultations =
                consultationRepository
                        .findByCaregiver_InstitutionAndConsultedAtBetween(
                                user.getInstitution(),
                                start,
                                end
                        );

        List<VisitSchedule> schedules =
                visitScheduleRepository
                        .findByCaregiver_InstitutionAndScheduledAtBetween(
                                user.getInstitution(),
                                start,
                                end
                        );

        long completedVisits = schedules.stream()
                .filter(schedule ->
                        schedule.getStatus()
                                == VisitScheduleStatus.COMPLETED
                )
                .count();

        long consultationCount =
                consultations.size();

        long abnormalDetectionCount =
                consultations.stream()
                        .filter(consultation ->
                                consultation.getStatus()
                                        != ConsultationStatus.NORMAL
                        )
                        .count();

        long riskTransitionCount =
                calculateRiskTransitionCount(
                        consultations,
                        start
                );

        List<AbnormalTrendItem> abnormalTrend =
                calculateAbnormalTrend(
                        consultations,
                        period,
                        range
                );

        return new StatisticsSummaryResponse(
                period,
                range.startDate(),
                range.endDate(),
                completedVisits,
                consultationCount,
                abnormalDetectionCount,
                riskTransitionCount,
                abnormalTrend
        );
    }

    private List<AbnormalTrendItem> calculateAbnormalTrend(
            List<Consultation> consultations,
            StatisticsPeriod period,
            StatisticsDateRange range
    ) {

        return switch (period) {

            case WEEK -> calculateWeeklyTrend(
                    consultations,
                    range
            );

            case MONTH -> calculateMonthlyTrend(
                    consultations,
                    range
            );

            case THREE_MONTHS,
                 SIX_MONTHS -> calculateMultiMonthTrend(
                    consultations,
                    range
            );

            case CUSTOM -> calculateCustomTrend(
                    consultations,
                    range
            );
        };
    }

    private List<AbnormalTrendItem> calculateWeeklyTrend(
            List<Consultation> consultations,
            StatisticsDateRange range
    ) {

        List<AbnormalTrendItem> result =
                new ArrayList<>();

        String[] labels = {
                "월", "화", "수", "목", "금", "토", "일"
        };

        for (int i = 0; i < 7; i++) {

            LocalDate date =
                    range.startDate().plusDays(i);

            long abnormalCount =
                    consultations.stream()
                            .filter(c ->
                                    c.getConsultedAt()
                                            .toLocalDate()
                                            .equals(date)
                            )
                            .filter(c ->
                                    c.getStatus()
                                            != ConsultationStatus.NORMAL
                            )
                            .count();

            long specialNoteCount =
                    consultations.stream()
                            .filter(c ->
                                    c.getConsultedAt()
                                            .toLocalDate()
                                            .equals(date)
                            )
                            .filter(c ->
                                    c.getStatus()
                                            == ConsultationStatus.SPECIAL_NOTE
                            )
                            .count();

            result.add(
                    new AbnormalTrendItem(
                            labels[i],
                            abnormalCount,
                            specialNoteCount
                    )
            );
        }
        return result;
    }

    private List<AbnormalTrendItem> calculateMonthlyTrend(
            List<Consultation> consultations,
            StatisticsDateRange range
    ) {

        List<AbnormalTrendItem> result = new ArrayList<>();

        LocalDate monthStart = range.startDate();
        LocalDate monthEnd = range.endDate();

        LocalDate weekStart = monthStart;
        int weekNumber = 1;

        while (!weekStart.isAfter(monthEnd)) {

            LocalDate weekEnd = weekStart.plusDays(6);

            if (weekEnd.isAfter(monthEnd)) {
                weekEnd = monthEnd;
            }

            LocalDate finalWeekStart = weekStart;
            LocalDate finalWeekEnd = weekEnd;

            long abnormalCount = consultations.stream()
                    .filter(c -> {
                        LocalDate date =
                                c.getConsultedAt().toLocalDate();

                        return !date.isBefore(finalWeekStart)
                                && !date.isAfter(finalWeekEnd);
                    })
                    .filter(c ->
                            c.getStatus()
                                    != ConsultationStatus.NORMAL
                    )
                    .count();

            long specialNoteCount = consultations.stream()
                    .filter(c -> {
                        LocalDate date =
                                c.getConsultedAt().toLocalDate();

                        return !date.isBefore(finalWeekStart)
                                && !date.isAfter(finalWeekEnd);
                    })
                    .filter(c ->
                            c.getStatus()
                                    == ConsultationStatus.SPECIAL_NOTE
                    )
                    .count();

            result.add(
                    new AbnormalTrendItem(
                            weekNumber + "주",
                            abnormalCount,
                            specialNoteCount
                    )
            );

            weekStart = weekEnd.plusDays(1);
            weekNumber++;
        }

        return result;
    }

    private List<AbnormalTrendItem> calculateMultiMonthTrend(
            List<Consultation> consultations,
            StatisticsDateRange range
    ) {

        List<AbnormalTrendItem> result = new ArrayList<>();

        LocalDate currentMonth =
                range.startDate().withDayOfMonth(1);

        LocalDate lastMonth =
                range.endDate().withDayOfMonth(1);

        while (!currentMonth.isAfter(lastMonth)) {

            int year = currentMonth.getYear();
            int month = currentMonth.getMonthValue();

            long abnormalCount = consultations.stream()
                    .filter(c ->
                            c.getConsultedAt().getYear() == year
                                    && c.getConsultedAt()
                                    .getMonthValue() == month
                    )
                    .filter(c ->
                            c.getStatus()
                                    != ConsultationStatus.NORMAL
                    )
                    .count();

            long specialNoteCount = consultations.stream()
                    .filter(c ->
                            c.getConsultedAt().getYear() == year
                                    && c.getConsultedAt()
                                    .getMonthValue() == month
                    )
                    .filter(c ->
                            c.getStatus()
                                    == ConsultationStatus.SPECIAL_NOTE
                    )
                    .count();

            result.add(
                    new AbnormalTrendItem(
                            month + "월",
                            abnormalCount,
                            specialNoteCount
                    )
            );

            currentMonth = currentMonth.plusMonths(1);
        }

        return result;
    }

    private List<AbnormalTrendItem> calculateCustomTrend(
            List<Consultation> consultations,
            StatisticsDateRange range
    ) {

        long days = ChronoUnit.DAYS.between(
                range.startDate(),
                range.endDate()
        ) + 1;

        // 31일 이하면 일 단위
        if (days <= 31) {
            return calculateDailyTrend(
                    consultations,
                    range
            );
        }

        // 그보다 길면 월 단위
        return calculateMultiMonthTrend(
                consultations,
                range
        );
    }

    private List<AbnormalTrendItem> calculateDailyTrend(
            List<Consultation> consultations,
            StatisticsDateRange range
    ) {

        List<AbnormalTrendItem> result = new ArrayList<>();

        LocalDate date = range.startDate();

        while (!date.isAfter(range.endDate())) {

            LocalDate currentDate = date;

            long abnormalCount = consultations.stream()
                    .filter(c ->
                            c.getConsultedAt()
                                    .toLocalDate()
                                    .equals(currentDate)
                    )
                    .filter(c ->
                            c.getStatus()
                                    != ConsultationStatus.NORMAL
                    )
                    .count();

            long specialNoteCount = consultations.stream()
                    .filter(c ->
                            c.getConsultedAt()
                                    .toLocalDate()
                                    .equals(currentDate)
                    )
                    .filter(c ->
                            c.getStatus()
                                    == ConsultationStatus.SPECIAL_NOTE
                    )
                    .count();

            result.add(
                    new AbnormalTrendItem(
                            date.getMonthValue()
                                    + "/"
                                    + date.getDayOfMonth(),
                            abnormalCount,
                            specialNoteCount
                    )
            );

            date = date.plusDays(1);
        }

        return result;
    }

    private long calculateRiskTransitionCount(
            List<Consultation> consultations,
            LocalDateTime periodStart
    ) {

        // 대상자별로 묶기
        Map<UUID, List<Consultation>> byRecipient =
                consultations.stream()
                        .filter(c -> c.getRecipient() != null)
                        .collect(Collectors.groupingBy(
                                c -> c.getRecipient().getId()
                        ));

        long transitionCount = 0;

        for (Map.Entry<UUID, List<Consultation>> entry
                : byRecipient.entrySet()) {

            UUID recipientId = entry.getKey();

            List<Consultation> recipientConsultations =
                    entry.getValue().stream()
                            .sorted(Comparator.comparing(
                                    Consultation::getConsultedAt
                            ))
                            .toList();

            // 조회 기간 시작 직전의 가장 최근 상담
            ConsultationStatus previousStatus =
                    consultationRepository
                            .findTopByRecipient_IdAndConsultedAtBeforeOrderByConsultedAtDesc(
                                    recipientId,
                                    periodStart
                            )
                            .map(Consultation::getStatus)
                            .orElse(null);

            for (Consultation consultation
                    : recipientConsultations) {

                ConsultationStatus currentStatus =
                        consultation.getStatus();

                // 이전에는 SPECIAL_NOTE가 아니었는데
                // 이번에 SPECIAL_NOTE가 됐으면 위험군 전환
                if (currentStatus == ConsultationStatus.SPECIAL_NOTE
                        && previousStatus
                        != ConsultationStatus.SPECIAL_NOTE) {

                    transitionCount++;
                }

                previousStatus = currentStatus;
            }
        }

        return transitionCount;
    }
}
