package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.request.ReactionRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.ReactionResponseDto;

public interface ReactionService {

    public ReactionResponseDto reactionForPost(ReactionRequestDto request, Long postId);

    public boolean cancelReaction(Long postId);

    public PaginationResponseDto getReactionsOfPost(PaginationRequestDto paginationRequestDto, Long postId);

}
