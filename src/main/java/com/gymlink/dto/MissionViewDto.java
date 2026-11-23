package com.gymlink.dto;

public record MissionViewDto(
        Long id,
        String title,
        String description,
        String frequency,
        int pointsReward,
        int xpReward,
        int currentProgress,
        int targetProgress,
        int progressPercent,
        boolean claimable,
        boolean claimed
) {}