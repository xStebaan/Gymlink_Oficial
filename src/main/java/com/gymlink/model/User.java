package com.gymlink.model;

import com.gymlink.model.enums.Role;
import com.gymlink.model.enums.VipRank;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 30)
    private String documentNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VipRank vipRank = VipRank.BRONCE;

    @Builder.Default
    private long xp = 0L;

    @Builder.Default
    private int pointsBalance = 0;

    @Builder.Default
    private int streakDays = 0;

    @Builder.Default
    private int shields = 1;

    @Builder.Default
    private int totalCheckins = 0;

    private LocalDate lastCheckInDate;

    @Column(length = 280)
    @Builder.Default
    private String bio = "Atleta comprometido en GymLink Club.";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getFirstName() {
        if (fullName == null || fullName.isBlank()) return "Atleta";
        return fullName.trim().split("\\s+")[0];
    }

    public String getInitials() {
        if (fullName == null || fullName.isBlank()) return "GL";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    public double getMultiplier() {
        return vipRank.getMultiplier();
    }
}