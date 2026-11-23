package com.gymlink.service;

import com.gymlink.dto.MemberHudDto;
import com.gymlink.model.CheckIn;
import com.gymlink.model.FeedPost;
import com.gymlink.model.User;
import com.gymlink.repository.CheckInRepository;
import com.gymlink.repository.FeedPostRepository;
import com.gymlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final FeedPostRepository feedPostRepository;
    private final GamificationService gamificationService;
    private final ChallengeService challengeService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public record CheckInLogDto(
            String timeFormatted,
            String memberName,
            String documentNumber,
            int streakAfter,
            int pointsAwarded
    ) {}

    @Transactional(readOnly = true)
    public MemberHudDto findFirstMemberForCheckIn(String query) {
        if (query == null || query.isBlank()) return null;
        return userRepository.searchMembersForCheckIn(query.trim())
                .stream()
                .findFirst()
                .map(MemberHudDto::from)
                .orElse(null);
    }

    @Transactional
    public int registerCheckIn(Long memberId) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Atleta no encontrado"));

        LocalDate today = LocalDate.now();
        boolean alreadyToday = !checkInRepository.findByUserAndCheckInTimeBetween(
                user, today.atStartOfDay(), today.atTime(23, 59, 59)).isEmpty();
        if (alreadyToday) {
            throw new IllegalStateException(user.getFullName() + " ya registró entrada hoy.");
        }

        double multiplier = user.getMultiplier();
        int awarded = gamificationService.applyCheckInRewards(user, 100);

        checkInRepository.save(CheckIn.builder()
                .user(user)
                .basePoints(100)
                .multiplierApplied(multiplier)
                .pointsAwarded(awarded)
                .xpAwarded(100)
                .streakAfter(user.getStreakDays())
                .checkInTime(LocalDateTime.now())
                .build());

        challengeService.incrementProgressForCheckIn(user);

        if (user.getStreakDays() == 7 || user.getStreakDays() == 14
                || user.getStreakDays() == 21 || user.getStreakDays() == 30) {
            feedPostRepository.save(FeedPost.builder()
                    .author(user)
                    .content("¡" + user.getFullName() + " alcanzó una racha imparable de "
                            + user.getStreakDays() + " días consecutivos!")
                    .tag("#RachaImparable")
                    .systemAchievement(true)
                    .build());
        }

        return awarded;
    }

    @Transactional(readOnly = true)
    public List<CheckInLogDto> getTodayCheckIns() {
        LocalDate today = LocalDate.now();
        return checkInRepository.findByCheckInTimeBetweenOrderByCheckInTimeDesc(
                        today.atStartOfDay(), today.atTime(23, 59, 59))
                .stream()
                .map(c -> new CheckInLogDto(
                        c.getCheckInTime().format(TIME_FMT),
                        c.getUser().getFullName(),
                        c.getUser().getDocumentNumber(),
                        c.getStreakAfter(),
                        c.getPointsAwarded()
                ))
                .toList();
    }
}