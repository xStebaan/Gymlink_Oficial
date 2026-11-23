package com.gymlink.repository;

import com.gymlink.model.Redemption;
import com.gymlink.model.User;
import com.gymlink.model.enums.RedemptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Long> {

    List<Redemption> findByUserOrderByRedeemedAtDesc(User user);

    List<Redemption> findAllByOrderByRedeemedAtDesc();

    long countByStatus(RedemptionStatus status);
}