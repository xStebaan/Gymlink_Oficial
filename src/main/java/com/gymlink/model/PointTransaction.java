package com.gymlink.model;

import com.gymlink.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String concept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private int amount;

    private boolean credit;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}