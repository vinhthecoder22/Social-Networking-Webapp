package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.entity.PostCategory;

public interface PostCategoryService {

    public PostCategory createPostCategory(String name);

    public void increaseInteractCategoryCount(Long postId);

    public void updateTrendingCategoryOnRedis(Long postId);
}
