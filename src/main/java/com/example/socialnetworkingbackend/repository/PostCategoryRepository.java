package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    Optional<PostCategory> findByName(String name);

    List<PostCategory> findTop5ByOrderByInteractionCountDesc();
}
