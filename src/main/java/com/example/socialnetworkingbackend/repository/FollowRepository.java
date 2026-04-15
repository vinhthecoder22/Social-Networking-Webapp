package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Follow;
import com.example.socialnetworkingbackend.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowingAndFollower(User following, User follower);

    @Modifying
    @Query("DELETE FROM Follow f WHERE f.following.id = :followingId AND f.follower.id = :followerId")
    int deleteByFollowingIdAndFollowerId(@Param("followingId") String followingId, @Param("followerId") String followerId);

    @EntityGraph(attributePaths = {"follower"})
    Page<Follow> findAllByFollowingId(String followingId, Pageable pageable);

    @EntityGraph(attributePaths = {"following"})
    Page<Follow> findAllByFollowerId(String followerId, Pageable pageable);
}