package com.gymlink.service;

import com.gymlink.dto.MissionViewDto;
import com.gymlink.model.Challenge;
import com.gymlink.model.MemberChallenge;
import com.gymlink.model.User;
import com.gymlink.model.enums.ChallengeFrequency;
import com.gymlink.model.enums.TransactionType;
import com.gymlink.repository.ChallengeRepository;
import com.gymlink.repository.MemberChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final MemberChallengeRepository memberChallengeRepository;
    private final GamificationService gamificationService;

    public record ChallengeAdminDto(
            Long id,
            String title,
            String description,
            String frequency,
            int targetCount,
            int pointsReward,
            int xpReward,
            boolean active
    ) {}

    public List<MissionViewDto> getMissionsForMember(User user) {
        List<Challenge> active = challengeRepository.findByActiveTrueOrderByIdDesc();
        return active.stream().map(ch -> {
            MemberChallenge mc = memberChallengeRepository.findByUserAndChallenge(user, ch)
                    .orElseGet(() -> MemberChallenge.builder().user(user).challenge(ch).currentProgress(0).build());

            int progress = Math.min(mc.getCurrentProgress(), ch.getTargetCount());
            int percent = ch.getTargetCount() > 0 ? (progress * 100) / ch.getTargetCount() : 0;
            boolean claimable = progress >= ch.getTargetCount() && !mc.isClaimed();

            return new MissionViewDto(
                    ch.getId(), ch.getTitle(), ch.getDescription(), ch.getFrequency().name(),
                    ch.getPointsReward(), ch.getXpReward(),
                    progress, ch.getTargetCount(), percent, claimable, mc.isClaimed()
            );
        }).toList();
    }

    @Transactional
    public void incrementProgressForCheckIn(User user) {
        for (Challenge ch : challengeRepository.findByActiveTrueOrderByIdDesc()) {
            MemberChallenge mc = memberChallengeRepository.findByUserAndChallenge(user, ch)
                    .orElseGet(() -> MemberChallenge.builder().user(user).challenge(ch).currentProgress(0).build());

            if (!mc.isClaimed() && mc.getCurrentProgress() < ch.getTargetCount()) {
                mc.setCurrentProgress(mc.getCurrentProgress() + 1);
                memberChallengeRepository.save(mc);
            }
        }
    }

    @Transactional
    public void claimMission(User user, Long challengeId) {
        Challenge ch = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Misión no encontrada"));

        MemberChallenge mc = memberChallengeRepository.findByUserAndChallenge(user, ch)
                .orElseThrow(() -> new IllegalStateException("Aún no tienes progreso en esta misión."));

        if (mc.isClaimed()) {
            throw new IllegalStateException("Ya reclamaste esta recompensa.");
        }
        if (mc.getCurrentProgress() < ch.getTargetCount()) {
            throw new IllegalStateException("Todavía no completas la meta de esta misión.");
        }

        mc.setClaimed(true);
        mc.setClaimedAt(LocalDateTime.now());
        memberChallengeRepository.save(mc);

        gamificationService.grantBonus(
                user, ch.getPointsReward(), ch.getXpReward(),
                "Misión Completada: " + ch.getTitle(), TransactionType.CHALLENGE
        );
    }

    public List<ChallengeAdminDto> getAllChallengesForAdmin() {
        return challengeRepository.findAllByOrderByIdDesc().stream()
                .map(c -> new ChallengeAdminDto(
                        c.getId(), c.getTitle(), c.getDescription(), c.getFrequency().name(),
                        c.getTargetCount(), c.getPointsReward(), c.getXpReward(), c.isActive()))
                .toList();
    }

    @Transactional
    public void saveChallenge(String title, String description, ChallengeFrequency frequency,
                              int targetCount, int pointsReward, int xpReward) {
        challengeRepository.save(Challenge.builder()
                .title(title.trim())
                .description(description.trim())
                .frequency(frequency)
                .targetCount(targetCount)
                .pointsReward(pointsReward)
                .xpReward(xpReward)
                .active(true)
                .build());
    }

    @Transactional
    public void toggleChallenge(Long id) {
        Challenge ch = challengeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reto no encontrado"));
        ch.setActive(!ch.isActive());
        challengeRepository.save(ch);
    }
}