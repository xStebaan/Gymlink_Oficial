package com.gymlink.dto;

public record RedemptionViewDto(
        Long id,
        String code,
        String digitalPin,
        String rewardName,
        String rewardType,
        boolean digital,
        int pointsSpent,
        String status,
        String memberName,
        String memberDocument,
        String redeemedAtFormatted,
        String createdAtFormatted
) {}