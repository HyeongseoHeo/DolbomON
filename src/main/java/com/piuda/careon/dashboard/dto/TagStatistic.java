package com.piuda.careon.dashboard.dto;

public record TagStatistic(
        String tag,
        long count,
        double percentage
) {
}
