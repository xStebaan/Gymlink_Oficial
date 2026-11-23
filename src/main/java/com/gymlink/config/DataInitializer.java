package com.gymlink.config;

import com.gymlink.model.*;
import com.gymlink.model.enums.*;
import com.gymlink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;
    private final ChallengeRepository challengeRepository;
    private final MemberChallengeRepository memberChallengeRepository;
    private final FeedPostRepository feedPostRepository;
    private final PointTransactionRepository transactionRepository;
    private final CheckInRepository checkInRepository;
    private final RedemptionRepository redemptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // 1. Usuarios Demo (Admin + Atletas)
        userRepository.save(User.builder()
                .fullName("Carlos Recepción")
                .email("admin@gymlink.fit")
                .documentNumber("90000001")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .vipRank(VipRank.TITAN)
                .build());

        User mateo = userRepository.save(User.builder()
                .fullName("Mateo Vargas")
                .email("atleta@gymlink.fit")
                .documentNumber("1020304050")
                .password(passwordEncoder.encode("123456"))
                .role(Role.MEMBER)
                .vipRank(VipRank.ORO)
                .xp(4850L)
                .pointsBalance(1420)
                .streakDays(14)
                .shields(2)
                .totalCheckins(62)
                .lastCheckInDate(LocalDate.now().minusDays(1))
                .bio("Enfocado en fuerza e hipertrofia. Meta 2026: Rango Titán.")
                .build());

        User daniel = userRepository.save(User.builder()
                .fullName("Daniel Gómez")
                .email("daniel@gymlink.fit")
                .documentNumber("1030405060")
                .password(passwordEncoder.encode("123456"))
                .role(Role.MEMBER)
                .vipRank(VipRank.TITAN)
                .xp(9850L)
                .pointsBalance(3200)
                .streakDays(38)
                .shields(3)
                .totalCheckins(120)
                .lastCheckInDate(LocalDate.now())
                .bio("Madrugador 5:30 AM. Sin excusas.")
                .build());

        User sofia = userRepository.save(User.builder()
                .fullName("Sofía Restrepo")
                .email("sofia@gymlink.fit")
                .documentNumber("1040506070")
                .password(passwordEncoder.encode("123456"))
                .role(Role.MEMBER)
                .vipRank(VipRank.ORO)
                .xp(7400L)
                .pointsBalance(2100)
                .streakDays(22)
                .shields(2)
                .totalCheckins(88)
                .lastCheckInDate(LocalDate.now())
                .bio("Cross-training y running club.")
                .build());

        // 2. Catálogo VIP
        Reward spotify = rewardRepository.save(Reward.builder()
                .name("1 Mes de Spotify Premium Individual")
                .description("PIN digital oficial para activar 30 días de música sin anuncios mientras entrenas.")
                .category(RewardCategory.DIGITAL)
                .type(RewardType.DIGITAL)
                .pointsCost(1800)
                .stock(50)
                .active(true)
                .build());

        Reward protein = rewardRepository.save(Reward.builder()
                .name("Proteína Whey Isolate 2lb")
                .description("Suplemento de rápida absorción sabor vainilla gourmet. Retiro en barra.")
                .category(RewardCategory.SUPLEMENTOS)
                .type(RewardType.FISICO)
                .pointsCost(2500)
                .stock(12)
                .active(true)
                .build());

        Reward shaker = rewardRepository.save(Reward.builder()
                .name("Shaker Metálico GymLink Edition")
                .description("Vaso mezclador acero inoxidable negro mate 700ml.")
                .category(RewardCategory.MERCH)
                .type(RewardType.FISICO)
                .pointsCost(650)
                .stock(20)
                .active(true)
                .build());

        rewardRepository.save(Reward.builder()
                .name("Pase Invitado VIP Fin de Semana")
                .description("Trae a un amigo a entrenar gratis sábado y domingo.")
                .category(RewardCategory.BENEFICIOS)
                .type(RewardType.DIGITAL)
                .pointsCost(400)
                .stock(100)
                .active(true)
                .build());

        // 3. Misiones y Retos
        Challenge c1 = challengeRepository.save(Challenge.builder()
                .title("Semana de Hierro (4 Entrenos)")
                .description("Registra 4 entradas en recepción durante esta semana.")
                .frequency(ChallengeFrequency.SEMANAL)
                .targetCount(4)
                .pointsReward(250)
                .xpReward(150)
                .active(true)
                .build());

        Challenge c2 = challengeRepository.save(Challenge.builder()
                .title("Maratón Mensual Titán")
                .description("Completa 16 check-ins en el mes para recibir bono élite.")
                .frequency(ChallengeFrequency.MENSUAL)
                .targetCount(16)
                .pointsReward(800)
                .xpReward(500)
                .active(true)
                .build());

        memberChallengeRepository.saveAll(List.of(
                MemberChallenge.builder().user(mateo).challenge(c1).currentProgress(4).claimed(false).build(),
                MemberChallenge.builder().user(mateo).challenge(c2).currentProgress(11).claimed(false).build()
        ));

        // 4. Movimientos e Historial
        transactionRepository.saveAll(List.of(
                PointTransaction.builder().user(mateo).concept("Check-in Diario (Multiplicador 1.5x)").type(TransactionType.CHECK_IN).amount(150).credit(true).build(),
                PointTransaction.builder().user(mateo).concept("Bono Racha 14 Días").type(TransactionType.BONUS).amount(300).credit(true).build(),
                PointTransaction.builder().user(mateo).concept("Canje: Shaker Metálico GymLink").type(TransactionType.REDEEM).amount(650).credit(false).build()
        ));

        // 5. Canjes de ejemplo en Billetera
        redemptionRepository.save(Redemption.builder()
                .user(mateo)
                .reward(spotify)
                .code("GL-9041")
                .digitalPin("SPTF-8841-GYM2")
                .pointsSpent(1800)
                .status(RedemptionStatus.ENTREGADO)
                .redeemedAt(LocalDateTime.now().minusDays(3))
                .deliveredAt(LocalDateTime.now().minusDays(3))
                .build());

        redemptionRepository.save(Redemption.builder()
                .user(mateo)
                .reward(shaker)
                .code("GL-7720")
                .pointsSpent(650)
                .status(RedemptionStatus.PENDIENTE)
                .redeemedAt(LocalDateTime.now().minusHours(5))
                .build());

        // 6. Check-ins iniciales para gráfica
        checkInRepository.save(CheckIn.builder().user(daniel).basePoints(100).multiplierApplied(2.0).pointsAwarded(200).xpAwarded(100).streakAfter(38).checkInTime(LocalDateTime.now().minusHours(2)).build());
        checkInRepository.save(CheckIn.builder().user(sofia).basePoints(100).multiplierApplied(1.5).pointsAwarded(150).xpAwarded(100).streakAfter(22).checkInTime(LocalDateTime.now().minusHours(1)).build());

        // 7. Publicaciones del Club Social
        FeedPost p1 = feedPostRepository.save(FeedPost.builder()
                .author(daniel)
                .content("¡Nuevo PR en sentadilla libre: 150kg x 3 reps! La disciplina de las 5:30 AM paga.")
                .tag("#ModoTitán")
                .systemAchievement(false)
                .createdAt(LocalDateTime.now().minusMinutes(35))
                .build());

        FeedPost p2 = feedPostRepository.save(FeedPost.builder()
                .author(mateo)
                .content("¡Mateo Vargas alcanzó una racha imparable de 14 días consecutivos entrenando!")
                .tag("#RachaImparable")
                .systemAchievement(true)
                .createdAt(LocalDateTime.now().minusHours(3))
                .build());

        // Silenciamos warning de variable sin usar si aplica
        protein.getId();
        p1.getId();
        p2.getId();
    }
}