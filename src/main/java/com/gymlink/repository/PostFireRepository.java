package com.gymlink.repository;

import com.gymlink.model.FeedPost;
import com.gymlink.model.PostFire;
import com.gymlink.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostFireRepository extends JpaRepository<PostFire, Long> {

    Optional<PostFire> findByPostAndUser(FeedPost post, User user);

    boolean existsByPostAndUser(FeedPost post, User user);
}