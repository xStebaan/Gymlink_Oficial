package com.gymlink.dto;

public record AdminKpiDto(
        long todayCheckins,
        long pointsIssuedToday,
        long pendingRedemptions,
        long activeStreakMembers
) {}