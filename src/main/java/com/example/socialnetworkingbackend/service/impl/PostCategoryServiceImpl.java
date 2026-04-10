package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.PostCategory;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.PostCategoryRepository;
import com.example.socialnetworkingbackend.repository.PostRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.PostCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostCategoryServiceImpl implements PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;
    private final PostRepository postRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PostCategory createPostCategory(String name) {
        PostCategory postCategory = postCategoryRepository.findByName(name).orElse(null);

        if (postCategory == null) {
            postCategory = new PostCategory();
            postCategory.setName(name);
        } else {
            postCategory.setInteractionCount(postCategory.getInteractionCount() + 1);
        }
        return postCategoryRepository.save(postCategory);
    }

    @Override
    public void increaseInteractCategoryCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)}));
        PostCategory postCategory = post.getCategory();
        postCategory.setInteractionCount(postCategory.getInteractionCount() + 1);
        postCategoryRepository.save(postCategory);
    }

    @Override
    public void updateTrendingCategoryOnRedis(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        log.info("Updating trending of user id {}", userPrincipal.getId());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ORIGINAL_POST,  new String[]{String.valueOf(postId)}));

        PostCategory postCategory = post.getCategory();
        String redisKey = "username:" + userPrincipal.getUsername() + ":trending";

        redisTemplate.opsForHash().increment(redisKey, postCategory.getName(), 1);

        log.info("Updated trending successfully");
    }
}