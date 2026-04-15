package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByRole_Id(Long roleId);

    default User getUser(UserPrincipal currentUser) {
        return findByUsernameOrEmail(currentUser.getUsername(), currentUser.getUsername())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                        new String[]{currentUser.getUsername()}));
    }

    Optional<User> findByProviderId(String providerId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.followerCount = u.followerCount + 1 WHERE u.id = :userId")
    void incrementFollowerCount(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.followerCount = u.followerCount - 1 WHERE u.id = :userId AND u.followerCount > 0")
    void decrementFollowerCount(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.followingCount = u.followingCount + 1 WHERE u.id = :userId")
    void incrementFollowingCount(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.followingCount = u.followingCount - 1 WHERE u.id = :userId AND u.followingCount > 0")
    void decrementFollowingCount(@Param("userId") String userId);
}
