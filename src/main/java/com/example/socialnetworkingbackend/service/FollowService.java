package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.request.FollowRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.FollowResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserSummaryDto;

public interface FollowService {

     FollowResponseDto follow(FollowRequestDto requestDto);

     boolean unfollow(String followingId);

     boolean removeFollower(String followerId);

     PaginationResponseDto<UserSummaryDto> getFollowers(PaginationRequestDto requestDto);

     PaginationResponseDto<UserSummaryDto> getFollowings(PaginationRequestDto requestDto);

}
