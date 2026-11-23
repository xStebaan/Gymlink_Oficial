package com.gymlink.service;

import com.gymlink.dto.RegisterRequest;
import com.gymlink.model.PointTransaction;
import com.gymlink.model.User;
import com.gymlink.model.enums.Role;
import com.gymlink.model.enums.TransactionType;
import com.gymlink.model.enums.VipRank;
import com.gymlink.repository.PointTransactionRepository;
import com.gymlink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PointTransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerMember(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail().trim())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo electrónico.");
        }
        if (userRepository.existsByDocumentNumber(request.getDocumentNumber().trim())) {
            throw new IllegalArgumentException("Ya existe un atleta registrado con ese documento/cédula.");
        }

        User newUser = userRepository.save(User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .documentNumber(request.getDocumentNumber().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.MEMBER)
                .vipRank(VipRank.BRONCE)
                .pointsBalance(100)
                .xp(100L)
                .shields(1)
                .build());

        transactionRepository.save(PointTransaction.builder()
                .user(newUser)
                .concept("Bono de Bienvenida GymLink")
                .type(TransactionType.BONUS)
                .amount(100)
                .credit(true)
                .build());
    }

    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Atleta autenticado no encontrado"));
    }
}