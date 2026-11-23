package com.gymlink.service;

import com.gymlink.model.User;
import com.gymlink.model.enums.VipRank;
import com.gymlink.repository.CheckInRepository;
import com.gymlink.repository.PointTransactionRepository;
import com.gymlink.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PointTransactionRepository transactionRepository;
    @Mock
    private CheckInRepository checkInRepository;

    @InjectMocks
    private GamificationService gamificationService;

    @Test
    void shouldApplyOroMultiplierAndConsumeShieldWhenOneDayMissed() {
        User user = User.builder()
                .fullName("Mateo Vargas")
                .vipRank(VipRank.ORO)
                .xp(3500L)
                .pointsBalance(100)
                .streakDays(10)
                .shields(2)
                .lastCheckInDate(LocalDate.now().minusDays(2))
                .build();

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        int awarded = gamificationService.applyCheckInRewards(user, 100);

        assertEquals(150, awarded);
        assertEquals(11, user.getStreakDays());
        assertEquals(1, user.getShields());
        assertEquals(250, user.getPointsBalance());
    }
}