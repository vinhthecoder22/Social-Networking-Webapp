package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = """
        SELECT * FROM post 
        WHERE MATCH(title, content) AGAINST (:keyword IN NATURAL LANGUAGE MODE)
    """, nativeQuery = true)
    Page<Post> searchByTitleKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Post> findByCategoryNameIn(List<String> categoryName, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user IN (SELECT f.following FROM Follow f WHERE f.follower = :currentUser) ORDER BY p.createdAt DESC")
    Page<Post> getNewsfeedForUser(@Param("currentUser") User currentUser, Pageable pageable);
}
