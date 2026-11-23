package com.gymlink.model;

import com.gymlink.model.enums.RedemptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "redemptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reward_id")
    private Reward reward;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    private String digitalPin;

    private int pointsSpent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RedemptionStatus status;

    @Builder.Default
    private LocalDateTime redeemedAt = LocalDateTime.now();

    private LocalDateTime deliveredAt;
}