package com.gymlink.repository;

import com.gymlink.model.CheckIn;
import com.gymlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    List<CheckIn> findByCheckInTimeBetweenOrderByCheckInTimeDesc(LocalDateTime start, LocalDateTime end);

    long countByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);

    List<CheckIn> findByUserAndCheckInTimeBetween(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.pointsAwarded), 0) FROM CheckIn c WHERE c.checkInTime BETWEEN :start AND :end")
    long sumPointsAwardedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}