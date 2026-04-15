package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PagingMeta;
import com.example.socialnetworkingbackend.domain.dto.request.FollowRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.FollowResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserSummaryDto;
import com.example.socialnetworkingbackend.domain.entity.Follow;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.FollowMapper;
import com.example.socialnetworkingbackend.domain.mapper.UserMapper;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.exception.ConflictException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.FollowRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public FollowResponseDto follow(FollowRequestDto requestDto) {
        String followingId = requestDto.getFollowingId();
        String followerId = getCurrentUserId();

        if (followerId.equals(followingId)) {
            throw new BadRequestException(ErrorMessage.Follow.ERR_FOLLOW_YOURSELF);
        }

        if (!userRepository.existsById(followingId)) {
            throw new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{followingId});
        }

        User followerProxy = userRepository.getReferenceById(followerId);
        User followingProxy = userRepository.getReferenceById(followingId);

        if (followRepository.existsByFollowingAndFollower(followingProxy, followerProxy)) {
            throw new ConflictException(ErrorMessage.Follow.ERR_DUPLICATE);
        }

        Follow newFollow = new Follow();
        newFollow.setFollowing(followingProxy);
        newFollow.setFollower(followerProxy);

        FollowResponseDto responseDto = followMapper.toFollowResponseDto(followRepository.save(newFollow));
        responseDto.setFollowerId(followerId);
        responseDto.setFollowingId(followingId);

        // Tăng người đang theo dõi của mình (+1 Following)
        userRepository.incrementFollowingCount(followerId);
        // Tăng người theo dõi của người kia (+1 Follower)
        userRepository.incrementFollowerCount(followingId);

        return responseDto;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public boolean unfollow(String followingId) {
        String followerId = getCurrentUserId();
        int deletedCount = followRepository.deleteByFollowingIdAndFollowerId(followingId, followerId);
        if (deletedCount == 0) {
            throw new BadRequestException(ErrorMessage.Follow.ERR_UNFOLLOW_USER, new String[]{followingId});
        }

        // Giảm người đang theo dõi của mình (-1 Following)
        userRepository.decrementFollowingCount(followerId);
        // Giảm người theo dõi của người kia (-1 Follower)
        userRepository.decrementFollowerCount(followingId);

        return true;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public boolean removeFollower(String followerId) {
        String followingId = getCurrentUserId();
        int deletedCount = followRepository.deleteByFollowingIdAndFollowerId(followingId, followerId);
        if (deletedCount == 0) {
            throw new BadRequestException(ErrorMessage.Follow.ERR_REMOVE_FOLLOWER, new String[]{followerId});
        }

        // Mình mất đi 1 người theo dõi (-1 Follower)
        userRepository.decrementFollowerCount(followingId);
        // Người ta bị mất 1 người đang theo dõi (-1 Following)
        userRepository.decrementFollowingCount(followerId);

        return true;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<UserSummaryDto> getFollowers(PaginationRequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.getPageNum(), requestDto.getPageSize());
        Page<Follow> followers = followRepository.findAllByFollowingId(getCurrentUserId(), pageable);

        List<UserSummaryDto> userSummaries = followers.map(f -> userMapper.toUserSummaryDto(f.getFollower())).getContent();
        return new PaginationResponseDto<>(buildPagingMeta(followers, requestDto), userSummaries);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<UserSummaryDto> getFollowings(PaginationRequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.getPageNum(), requestDto.getPageSize());
        Page<Follow> followings = followRepository.findAllByFollowerId(getCurrentUserId(), pageable);

        List<UserSummaryDto> userSummaries = followings.map(f -> userMapper.toUserSummaryDto(f.getFollowing())).getContent();
        return new PaginationResponseDto<>(buildPagingMeta(followings, requestDto), userSummaries);
    }

    private String getCurrentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    private PagingMeta buildPagingMeta(Page<?> page, PaginationRequestDto requestDto) {
        return PagingMeta.builder()
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageNum(requestDto.getPageNum())
                .pageSize(requestDto.getPageSize())
                .build();
    }
}