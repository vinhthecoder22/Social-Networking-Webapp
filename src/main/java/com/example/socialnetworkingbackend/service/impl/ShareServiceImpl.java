package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.PostStatusConstant;
import com.example.socialnetworkingbackend.domain.dto.request.SharePostRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.PostSummaryDto;
import com.example.socialnetworkingbackend.domain.dto.response.SharePostResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserSummaryDto;
import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.MediaMapper;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.PostRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaMapper mediaMapper;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Override
    public SharePostResponseDto sharePost(Long postId, SharePostRequestDto request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User currentUser = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[] { username }));

        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ORIGINAL_POST));

        // Chống Share lồng nhau (Inception)
        // Nếu user share lại 1 bài đã được share => trỏ thẳng về bài gốc tận cùng
        Post rootPost = originalPost.getOriginalPost() != null ? originalPost.getOriginalPost() : originalPost;

        postRepository.incrementShareCount(rootPost.getId());

        long updatedShareCount = postRepository.findById(rootPost.getId())
                .map(Post::getShareCount).orElse(rootPost.getShareCount() + 1);

        PostSummaryDto postSummaryDto = new PostSummaryDto();
        postSummaryDto.setTitle(rootPost.getTitle());
        postSummaryDto.setContent(rootPost.getContent());
        postSummaryDto.setShareCount(updatedShareCount);

        UserSummaryDto originalAuthor = new UserSummaryDto(
                rootPost.getUser().getId(),
                rootPost.getUser().getFirstName(),
                rootPost.getUser().getLastName(),
                rootPost.getUser().getImageUrl()
        );
        postSummaryDto.setCreatedBy(originalAuthor);

        if (rootPost.getMediaList() != null) {
            postSummaryDto.setMediaList(rootPost.getMediaList().stream()
                    .map(mediaMapper::toMediaResponseDto)
                    .collect(Collectors.toList()));
        }

        // Tạo bài viết Share mới
        Post newPost = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .originalPost(rootPost)
                .mediaType(rootPost.getMediaType())
                .user(currentUser)
                .status(PostStatusConstant.PENDING_MODERATION)
                .reactionCount(0L)
                .commentCount(0L)
                .shareCount(0L)
                .build();

        newPost = postRepository.save(newPost);

        // Đóng gói Response
        SharePostResponseDto responseDto = new SharePostResponseDto();
        responseDto.setId(newPost.getId());
        responseDto.setTitle(newPost.getTitle());
        responseDto.setContent(newPost.getContent());
        responseDto.setCreatedBy(currentUser.getUsername());
        responseDto.setOriginalPost(postSummaryDto);

        return responseDto;
    }

}
