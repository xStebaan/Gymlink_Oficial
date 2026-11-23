package com.gymlink.service;

import com.gymlink.model.Redemption;
import com.gymlink.model.Reward;
import com.gymlink.model.User;
import com.gymlink.model.enums.RedemptionStatus;
import com.gymlink.model.enums.RewardCategory;
import com.gymlink.model.enums.RewardType;
import com.gymlink.repository.PointTransactionRepository;
import com.gymlink.repository.RedemptionRepository;
import com.gymlink.repository.RewardRepository;
import com.gymlink.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedemptionServiceTest {

    @Mock
    private RedemptionRepository redemptionRepository;
    @Mock
    private RewardRepository rewardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointTransactionRepository transactionRepository;

    @InjectMocks
    private RedemptionService redemptionService;

    @Test
    void shouldGenerateDigitalPinAndDeductPointsAndStock() {
        User user = User.builder().id(1L).fullName("Mateo").pointsBalance(2000).build();
        Reward reward = Reward.builder()
                .id(10L)
                .name("Spotify Premium")
                .category(RewardCategory.DIGITAL)
                .type(RewardType.DIGITAL)
                .pointsCost(1800)
                .stock(5)
                .active(true)
                .build();

        when(rewardRepository.findById(10L)).thenReturn(Optional.of(reward));
        when(redemptionRepository.save(any(Redemption.class))).thenAnswer(inv -> inv.getArgument(0));

        Redemption result = redemptionService.redeemReward(user, 10L);

        assertEquals(200, user.getPointsBalance());
        assertEquals(4, reward.getStock());
        assertEquals(RedemptionStatus.ENTREGADO, result.getStatus());
        assertNotNull(result.getDigitalPin());
        assertTrue(result.getCode().startsWith("GL-"));
    }
}