package com.gymlink.dto;

import com.gymlink.model.User;

public record MemberHudDto(
        Long id,
        String fullName,
        String firstName,
        String initials,
        String email,
        String documentNumber,
        String vipRank,
        double multiplier,
        long xp,
        int pointsBalance,
        int streakDays,
        int shields,
        int totalCheckins,
        String bio
) {
    public static MemberHudDto from(User u) {
        return new MemberHudDto(
                u.getId(),
                u.getFullName(),
                u.getFirstName(),
                u.getInitials(),
                u.getEmail(),
                u.getDocumentNumber(),
                u.getVipRank().name(),
                u.getMultiplier(),
                u.getXp(),
                u.getPointsBalance(),
                u.getStreakDays(),
                u.getShields(),
                u.getTotalCheckins(),
                u.getBio()
        );
    }
}