package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findParentCommentsByPostId(@Param("postId") Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByParentId(Long parentId);

    @EntityGraph(attributePaths = {"replies", "replies.user", "user"})
    Optional<Comment> findByIdAndParentIsNull(Long id);

    @EntityGraph(attributePaths = {"user", "post"})
    @Query("SELECT c FROM Comment c WHERE c.id = :id")
    Optional<Comment> findByIdWithUserAndPost(@Param("id") Long id);


}
