package com.gymlink.service;

import com.gymlink.dto.WeeklyDayDto;
import com.gymlink.model.CheckIn;
import com.gymlink.model.PointTransaction;
import com.gymlink.model.User;
import com.gymlink.model.enums.TransactionType;
import com.gymlink.model.enums.VipRank;
import com.gymlink.repository.CheckInRepository;
import com.gymlink.repository.PointTransactionRepository;
import com.gymlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserRepository userRepository;
    private final PointTransactionRepository transactionRepository;
    private final CheckInRepository checkInRepository;

    @Transactional
    public int applyCheckInRewards(User user, int basePoints) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastCheckInDate();

        if (last == null) {
            user.setStreakDays(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(last, today);
            if (daysBetween == 1) {
                user.setStreakDays(user.getStreakDays() + 1);
            } else if (daysBetween == 2 && user.getShields() > 0) {
                user.setShields(user.getShields() - 1);
                user.setStreakDays(user.getStreakDays() + 1);
            } else if (daysBetween > 1) {
                user.setStreakDays(1);
            }
        }

        user.setLastCheckInDate(today);
        user.setTotalCheckins(user.getTotalCheckins() + 1);

        double multiplier = user.getMultiplier();
        int awardedPoints = (int) Math.round(basePoints * multiplier);
        int xpGain = 100;

        user.setPointsBalance(user.getPointsBalance() + awardedPoints);
        user.setXp(user.getXp() + xpGain);
        user.setVipRank(VipRank.fromXp(user.getXp()));
        userRepository.save(user);

        transactionRepository.save(PointTransaction.builder()
                .user(user)
                .concept("Check-in Diario (Multiplicador " + multiplier + "x)")
                .type(TransactionType.CHECK_IN)
                .amount(awardedPoints)
                .credit(true)
                .build());

        return awardedPoints;
    }

    @Transactional
    public void grantBonus(User user, int points, int xp, String concept, TransactionType type) {
        if (points > 0) {
            user.setPointsBalance(user.getPointsBalance() + points);
            transactionRepository.save(PointTransaction.builder()
                    .user(user)
                    .concept(concept)
                    .type(type)
                    .amount(points)
                    .credit(true)
                    .build());
        }
        if (xp > 0) {
            user.setXp(user.getXp() + xp);
            user.setVipRank(VipRank.fromXp(user.getXp()));
        }
        userRepository.save(user);
    }

    public List<WeeklyDayDto> getWeeklyTracker(User user) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate sunday = today.with(DayOfWeek.SUNDAY);

        List<CheckIn> weekCheckIns = checkInRepository.findByUserAndCheckInTimeBetween(
                user, monday.atStartOfDay(), sunday.atTime(23, 59, 59));

        Set<LocalDate> attendedDates = weekCheckIns.stream()
                .map(c -> c.getCheckInTime().toLocalDate())
                .collect(Collectors.toSet());

        String[] labels = {"L", "M", "X", "J", "V", "S", "D"};
        List<WeeklyDayDto> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            boolean done = attendedDates.contains(d);
            result.add(new WeeklyDayDto(labels[i], done, false, d.equals(today)));
        }
        return result;
    }
}