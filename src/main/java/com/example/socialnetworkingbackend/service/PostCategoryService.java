package com.example.projectbase.service;

import com.example.projectbase.domain.entity.PostCategory;

public interface PostCategoryService {

    public PostCategory createPostCategory(String name);

    public void increaseInteractCategoryCount(Long postId);

    public void updateTrendingCategoryOnRedis(Long postId);
}
