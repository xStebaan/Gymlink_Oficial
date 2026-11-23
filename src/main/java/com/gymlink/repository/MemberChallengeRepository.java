package com.gymlink.repository;

import com.gymlink.model.Challenge;
import com.gymlink.model.MemberChallenge;
import com.gymlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberChallengeRepository extends JpaRepository<MemberChallenge, Long> {

    Optional<MemberChallenge> findByUserAndChallenge(User user, Challenge challenge);

    List<MemberChallenge> findByUser(User user);
}