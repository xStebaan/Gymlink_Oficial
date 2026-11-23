package com.gymlink.dto;

public record WeeklyDayDto(
        String shortName,
        boolean completed,
        boolean shielded,
        boolean today
) {}