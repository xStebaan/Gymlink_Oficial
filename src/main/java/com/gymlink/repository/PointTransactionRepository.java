package com.gymlink.repository;

import com.gymlink.model.PointTransaction;
import com.gymlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM PointTransaction t WHERE t.user = :user AND t.credit = :credit")
    long sumAmountByUserAndCredit(@Param("user") User user, @Param("credit") boolean credit);
}