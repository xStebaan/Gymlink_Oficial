package com.gymlink.repository;

import com.gymlink.model.User;
import com.gymlink.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByDocumentNumber(String documentNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDocumentNumber(String documentNumber);

    List<User> findTop10ByRoleOrderByXpDesc(Role role);

    List<User> findByRoleOrderByFullNameAsc(Role role);

    long countByRoleAndStreakDaysGreaterThanEqual(Role role, int minStreak);

    @Query("""
           SELECT u FROM User u
           WHERE u.role = 'MEMBER'
             AND (u.documentNumber = :q OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY u.fullName ASC
           """)
    List<User> searchMembersForCheckIn(@Param("q") String query);
}