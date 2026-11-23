package com.gymlink.service;

import com.gymlink.dto.RedemptionViewDto;
import com.gymlink.model.PointTransaction;
import com.gymlink.model.Redemption;
import com.gymlink.model.Reward;
import com.gymlink.model.User;
import com.gymlink.model.enums.RedemptionStatus;
import com.gymlink.model.enums.RewardType;
import com.gymlink.model.enums.TransactionType;
import com.gymlink.repository.PointTransactionRepository;
import com.gymlink.repository.RedemptionRepository;
import com.gymlink.repository.RewardRepository;
import com.gymlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedemptionService {

    private final RedemptionRepository redemptionRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;
    private final PointTransactionRepository transactionRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Transactional
    public Redemption redeemReward(User user, Long rewardId) {
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new IllegalArgumentException("Premio no encontrado."));

        if (!reward.isActive() || reward.getStock() <= 0) {
            throw new IllegalStateException("Este premio está agotado o no disponible.");
        }
        if (user.getPointsBalance() < reward.getPointsCost()) {
            throw new IllegalStateException("No tienes suficientes puntos para este premio.");
        }

        user.setPointsBalance(user.getPointsBalance() - reward.getPointsCost());
        reward.setStock(reward.getStock() - 1);
        userRepository.save(user);
        rewardRepository.save(reward);

        boolean isDigital = reward.getType() == RewardType.DIGITAL;
        String code = "GL-" + (1000 + RANDOM.nextInt(9000));
        String pin = isDigital ? generateDigitalPin() : null;

        Redemption redemption = redemptionRepository.save(Redemption.builder()
                .user(user)
                .reward(reward)
                .code(code)
                .digitalPin(pin)
                .pointsSpent(reward.getPointsCost())
                .status(isDigital ? RedemptionStatus.ENTREGADO : RedemptionStatus.PENDIENTE)
                .redeemedAt(LocalDateTime.now())
                .deliveredAt(isDigital ? LocalDateTime.now() : null)
                .build());

        transactionRepository.save(PointTransaction.builder()
                .user(user)
                .concept("Canje VIP: " + reward.getName())
                .type(TransactionType.REDEEM)
                .amount(reward.getPointsCost())
                .credit(false)
                .build());

        return redemption;
    }

    @Transactional(readOnly = true)
    public List<RedemptionViewDto> getActivePassesForMember(User user) {
        return redemptionRepository.findByUserOrderByRedeemedAtDesc(user)
                .stream()
                .filter(r -> r.getReward().getType() == RewardType.DIGITAL
                        || r.getStatus() == RedemptionStatus.PENDIENTE)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RedemptionViewDto> getPastPassesForMember(User user) {
        return redemptionRepository.findByUserOrderByRedeemedAtDesc(user)
                .stream()
                .filter(r -> r.getReward().getType() == RewardType.FISICO
                        && r.getStatus() == RedemptionStatus.ENTREGADO)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RedemptionViewDto> getAllRedemptionsForAdmin() {
        return redemptionRepository.findAllByOrderByRedeemedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deliverPhysicalRedemption(Long id) {
        Redemption redemption = redemptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Canje no encontrado."));
        redemption.setStatus(RedemptionStatus.ENTREGADO);
        redemption.setDeliveredAt(LocalDateTime.now());
        redemptionRepository.save(redemption);
    }

    private RedemptionViewDto toDto(Redemption r) {
        boolean isDigital = r.getReward().getType() == RewardType.DIGITAL;
        String dateText = r.getRedeemedAt().format(DATE_FMT);
        return new RedemptionViewDto(
                r.getId(),
                r.getCode(),
                r.getDigitalPin(),
                r.getReward().getName(),
                r.getReward().getType().name(),
                isDigital,
                r.getPointsSpent(),
                r.getStatus().name(),
                r.getUser().getFullName(),
                r.getUser().getDocumentNumber(),
                dateText,
                dateText
        );
    }

    private String generateDigitalPin() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("SPTF-");
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        sb.append("-");
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        return sb.toString();
    }
}