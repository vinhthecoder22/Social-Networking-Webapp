package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
        SELECT * FROM post 
        WHERE MATCH(title, content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
    """, nativeQuery = true)
    Page<Post> searchByTitleKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Post> findByCategoryNameIn(List<String> categoryName, Pageable pageable);

    // Step 1: Get paginated post IDs using UNION ALL
    @Query(value = """
            SELECT p.id FROM post p
            INNER JOIN follow f ON f.following_id = p.user_id
            WHERE f.follower_id = :userId AND p.delete_flag = false
            UNION ALL
            SELECT p.id FROM post p
            WHERE p.user_id = :userId AND p.delete_flag = false
            ORDER BY id DESC
            """, countQuery = """
            SELECT COUNT(*) FROM (
                SELECT p.id FROM post p
                INNER JOIN follow f ON f.following_id = p.user_id
                WHERE f.follower_id = :userId AND p.delete_flag = false
                UNION ALL
                SELECT p.id FROM post p
                WHERE p.user_id = :userId AND p.delete_flag = false
            ) t
            """, nativeQuery = true)
    Page<Long> getNewsfeedPostIds(@Param("userId") String userId, Pageable pageable);

    // Step 2: Hydrate posts by IDs with eager fetching
    @EntityGraph(attributePaths = { "user", "mediaList", "category" })
    @Query("SELECT p FROM Post p WHERE p.id IN :ids ORDER BY p.createdAt DESC")
    List<Post> findPostsWithDetailsByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionCount = p.reactionCount + 1 WHERE p.id = :postId")
    void incrementReactionCount(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.reactionCount = p.reactionCount - 1 WHERE p.id = :postId AND p.reactionCount > 0")
    void decrementReactionCount(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - :count WHERE p.id = :postId AND p.commentCount >= :count")
    void decrementCommentCountBy(@Param("postId") Long postId, @Param("count") int count);

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.shareCount = p.shareCount + 1 WHERE p.id = :postId")
    void incrementShareCount(@Param("postId") Long postId);
}
