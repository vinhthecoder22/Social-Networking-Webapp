package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.Reaction;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Reaction findByUser_IdAndPost_Id(String userId, Long postId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Reaction r WHERE r.user.id = :userId AND r.post.id = :postId")
    int deleteByUserIdAndPostId(@Param("userId") String userId, @Param("postId") Long postId);

    Page<Reaction> findAllByPost(Post post, Pageable pageable);
}
