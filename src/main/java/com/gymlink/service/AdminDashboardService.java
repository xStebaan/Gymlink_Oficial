package com.gymlink.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymlink.dto.AdminKpiDto;
import com.gymlink.model.enums.RedemptionStatus;
import com.gymlink.model.enums.Role;
import com.gymlink.repository.CheckInRepository;
import com.gymlink.repository.RedemptionRepository;
import com.gymlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final CheckInRepository checkInRepository;
    private final RedemptionRepository redemptionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AdminKpiDto getKpis() {
        LocalDate today = LocalDate.now();
        long todayCheckins = checkInRepository.countByCheckInTimeBetween(
                today.atStartOfDay(), today.atTime(23, 59, 59));
        long pointsIssuedToday = checkInRepository.sumPointsAwardedBetween(
                today.atStartOfDay(), today.atTime(23, 59, 59));
        long pendingRedemptions = redemptionRepository.countByStatus(RedemptionStatus.PENDIENTE);
        long activeStreakMembers = userRepository.countByRoleAndStreakDaysGreaterThanEqual(Role.MEMBER, 3);

        return new AdminKpiDto(todayCheckins, pointsIssuedToday, pendingRedemptions, activeStreakMembers);
    }

    public String getWeeklyLabelsJson() {
        List<String> labels = new ArrayList<>();
        Locale es = Locale.forLanguageTag("es-CO");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            String shortDay = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, es);
            labels.add(shortDay.substring(0, 1).toUpperCase() + shortDay.substring(1));
        }
        return toJson(labels);
    }

    public String getWeeklyValuesJson() {
        List<Long> values = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            values.add(checkInRepository.countByCheckInTimeBetween(
                    d.atStartOfDay(), d.atTime(23, 59, 59)));
        }
        return toJson(values);
    }

    private String toJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}