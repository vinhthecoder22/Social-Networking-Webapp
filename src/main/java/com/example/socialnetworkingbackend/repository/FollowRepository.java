package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Follow;
import com.example.socialnetworkingbackend.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowingAndFollower(User following, User follower);

    Optional<Follow> findByFollowingAndFollower(User following, User follower);

    Page<Follow> findAllByFollower(User follower, Pageable pageable);

    Page<Follow> findAllByFollowing(User following, Pageable pageable);
}
