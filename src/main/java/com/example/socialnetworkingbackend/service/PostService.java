package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationFullRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.request.PostRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.PostResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface PostService {

    PostResponseDto createPost(PostRequestDto requestDto, List<File> files, List<String> contentTypeList) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException;

    void deletePost(Long postId);

    PaginationResponseDto<PostResponseDto> getAllPostsByTitleKeyword(PaginationFullRequestDto request);

    PostResponseDto getPostById(Long postId);

    PaginationResponseDto<PostResponseDto> getPostsTrendingForUser(PaginationFullRequestDto request) throws JsonProcessingException;

    PaginationResponseDto<PostResponseDto> getNewsfeed(PaginationRequestDto requestDto);
}

