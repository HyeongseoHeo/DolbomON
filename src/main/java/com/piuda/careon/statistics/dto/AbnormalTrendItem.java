package com.piuda.careon.statistics.dto;

public record AbnormalTrendItem(
        String label,
        long abnormalCount,
        long specialNoteCount
) {
}