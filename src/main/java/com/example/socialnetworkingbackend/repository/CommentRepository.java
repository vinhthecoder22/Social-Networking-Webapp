package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Lấy danh sách Root Comments (Cursor Pagination - Mới nhất lên đầu)
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL " +
            "AND (:cursor IS NULL OR c.id < :cursor) ORDER BY c.id DESC")
    List<Comment> findParentCommentsWithCursor(@Param("postId") Long postId, @Param("cursor") Long cursor, Pageable pageable);

    // Lấy danh sách Replies (Cursor Pagination - Cũ nhất lên đầu)
    @EntityGraph(attributePaths = {"user", "replyToUser"})
    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId " +
            "AND (:cursor IS NULL OR c.id > :cursor) ORDER BY c.id ASC")
    List<Comment> findRepliesWithCursor(@Param("parentId") Long parentId, @Param("cursor") Long cursor, Pageable pageable);

    // Tăng/Giảm số lượng reply trực tiếp trên DB để tránh Race Condition
    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + :amount WHERE c.id = :commentId")
    void updateReplyCount(@Param("commentId") Long commentId, @Param("amount") int amount);

    @EntityGraph(attributePaths = {"user", "post"})
    @Query("SELECT c FROM Comment c WHERE c.id = :id")
    Optional<Comment> findByIdWithUserAndPost(@Param("id") Long id);
}