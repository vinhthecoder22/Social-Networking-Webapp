package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.RestData;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.request.FollowRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.FollowResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserSummaryDto;
import com.example.socialnetworkingbackend.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;

@RestApiV1
@Log4j2
@RequiredArgsConstructor
@Tag(name = "Follow", description = "Các chức năng liên quan tới follow")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "Theo dõi người dùng khác", description = "Truyền vào ID của người muốn theo dõi")
    @PostMapping(value = UrlConstant.Follow.EXECUTING_FOLLOW)
    public ResponseEntity<RestData<?>> follow(@RequestBody @Valid FollowRequestDto requestDto) {
        FollowResponseDto response = followService.follow(requestDto);
        return VsResponseUtil.success(response);
    }

    @Operation(summary = "Unfollow người mình đã follow", description = "followingID -> người được follow")
    @PostMapping(value = UrlConstant.Follow.UNFOLLOW)
    public ResponseEntity<RestData<?>> unfollow(@RequestParam String followingId) {
        followService.unfollow(followingId);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "bỏ follow của người đã follow mình", description = "followerID -> Người follow mình")
    @PostMapping(value = UrlConstant.Follow.REMOVE_FOLLOWER)
    public ResponseEntity<RestData<?>> removeFollower(@RequestParam String followerId) {
        followService.removeFollower(followerId);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT);
    }

    @GetMapping(UrlConstant.Follow.GET_FOLLOWERS)
    public ResponseEntity<RestData<?>> getFollowers(@ParameterObject PaginationRequestDto requestDto) {
        PaginationResponseDto<UserSummaryDto> responseDto = followService.getFollowers(requestDto);
        return VsResponseUtil.success(responseDto);
    }

    @GetMapping(UrlConstant.Follow.GET_FOLLOWINGS)
    public ResponseEntity<RestData<?>> getFollowings(@ParameterObject PaginationRequestDto requestDto) {
        PaginationResponseDto<UserSummaryDto> responseDto = followService.getFollowings(requestDto);
        return VsResponseUtil.success(responseDto);
    }

}
