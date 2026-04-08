package com.example.socialnetworkingbackend.repository;

import com.example.socialnetworkingbackend.domain.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    List<Media> findAllByPublicIdIn(List<String> publicIds);

    void deleteAllByPublicIdIn(List<String> publicIds);

    Optional<Media> findMediaByPublicId(String publicId);
}
