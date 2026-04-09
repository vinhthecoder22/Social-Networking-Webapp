package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.domain.entity.PostCategory;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.PostCategoryRepository;
import com.example.projectbase.repository.PostRepository;
import com.example.projectbase.security.UserPrincipal;
import com.example.projectbase.service.PostCategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostCategoryServiceImpl implements PostCategoryService {

    private final PostCategoryRepository postCategoryRepository;
    private final PostRepository postRepository;
    private final RedisServiceImpl redisService;
    private final ObjectMapper objectMapper;

    @Override
    public PostCategory createPostCategory(String name) {
        PostCategory postCategory = postCategoryRepository.findByName(name);
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
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            log.info("Updating trending of user id {}", userPrincipal.getId());
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ORIGINAL_POST,  new String[]{String.valueOf(postId)}));

            PostCategory postCategory = post.getCategory();

            String trendingOfUser = redisService.get("username:" + authentication.getName() + ":trending");
            if (trendingOfUser == null) {
                Map<String, Object> dataTrending = new HashMap<>();
                dataTrending.put(postCategory.getName(), 1);
                String json = objectMapper.writeValueAsString(dataTrending);
                redisService.save("username:"+userPrincipal.getUsername()+":trending", json);
            } else {
                Map<String, Object> categoryPostList = objectMapper.readValue(trendingOfUser, new TypeReference<>() {});
                int interactCategoryCount = (int) categoryPostList.get(postCategory.getName());
                categoryPostList.put(post.getCategory().getName(), interactCategoryCount + 1);
                String updatedJson = objectMapper.writeValueAsString(categoryPostList);
                redisService.save("username:"+userPrincipal.getUsername()+":trending", updatedJson);
            }
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        log.info("Updated trending successfully");
    }
}
