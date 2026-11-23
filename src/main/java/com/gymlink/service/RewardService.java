package com.gymlink.service;

import com.gymlink.model.Reward;
import com.gymlink.model.enums.RewardCategory;
import com.gymlink.model.enums.RewardType;
import com.gymlink.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    public record RewardViewDto(
            Long id,
            String name,
            String description,
            String category,
            String type,
            int pointsCost,
            int stock,
            boolean active
    ) {
        public static RewardViewDto from(Reward r) {
            return new RewardViewDto(
                    r.getId(), r.getName(), r.getDescription(),
                    r.getCategory().name(), r.getType().name(),
                    r.getPointsCost(), r.getStock(), r.isActive()
            );
        }
    }

    public List<RewardViewDto> getActiveRewardsForShop() {
        return rewardRepository.findByActiveTrueOrderByPointsCostAsc()
                .stream().map(RewardViewDto::from).toList();
    }

    public List<RewardViewDto> getAllRewardsForAdmin() {
        return rewardRepository.findAllByOrderByIdDesc()
                .stream().map(RewardViewDto::from).toList();
    }

    public RewardViewDto getTargetRewardForDashboard() {
        return rewardRepository.findByActiveTrueOrderByPointsCostAsc()
                .stream()
                .filter(r -> r.getCategory() == RewardCategory.DIGITAL)
                .findFirst()
                .map(RewardViewDto::from)
                .orElse(null);
    }

    @Transactional
    public void saveReward(Long id, String name, String description,
                           RewardCategory category, RewardType type, int pointsCost, int stock) {
        Reward reward = (id != null)
                ? rewardRepository.findById(id).orElse(new Reward())
                : new Reward();

        reward.setName(name.trim());
        reward.setDescription(description.trim());
        reward.setCategory(category);
        reward.setType(type);
        reward.setPointsCost(pointsCost);
        reward.setStock(stock);
        if (id == null) reward.setActive(true);

        rewardRepository.save(reward);
    }

    @Transactional
    public void toggleActive(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Premio no encontrado"));
        reward.setActive(!reward.isActive());
        rewardRepository.save(reward);
    }
}