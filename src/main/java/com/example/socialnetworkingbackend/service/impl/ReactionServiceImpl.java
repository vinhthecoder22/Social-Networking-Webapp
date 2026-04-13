package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.ReactionTypeConstant;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PagingMeta;
import com.example.socialnetworkingbackend.domain.dto.request.ReactionRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.ReactionResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.Reaction;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.ReactionMapper;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.PostRepository;
import com.example.socialnetworkingbackend.repository.ReactionRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private static final Logger log = LogManager.getLogger(ReactionServiceImpl.class);
    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final ReactionMapper reactionMapper;
    private final UserRepository userRepository;

    @Transactional
    @PreAuthorize("isAuthenticated()")
    @Override
    public ReactionResponseDto reactionForPost(ReactionRequestDto request, Long postId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Kiểm tra User và Post (existsById => tối ưu RAM)
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(
                () -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userPrincipal.getId()})
        );
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)});
        }

        // Xử lý Reaction
        Reaction reaction = reactionRepository.findByUser_IdAndPost_Id(userPrincipal.getId(), postId);
        boolean isNewReaction = false;

        if (reaction == null) {
            reaction = new Reaction();
            isNewReaction = true;

            // Chỉ gán User và Post khi tạo mới để tránh gán lại dư thừa
            Post postProxy = postRepository.getReferenceById(postId); // Dùng proxy để gán ID mà không cần fetch DB
            reaction.setPost(postProxy);
            reaction.setUser(user);
        }

        try {
            ReactionTypeConstant type = ReactionTypeConstant.valueOf(request.getReactionType().toUpperCase());
            reaction.setReactionType(type);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Loại cảm xúc không hợp lệ");
        }

        Reaction savedReaction = reactionRepository.save(reaction);

        if (isNewReaction) {
            postRepository.incrementReactionCount(postId);
        }

        return reactionMapper.toReactionResponseDto(savedReaction);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    @Override
    public boolean cancelReaction(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (!postRepository.existsById(postId)) {
            throw new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)});
        }

        long response = reactionRepository.deleteByUserIdAndPostId(userPrincipal.getId(), postId);

        if (response == 0) {
            throw new NotFoundException(ErrorMessage.Reaction.ERR_NOT_FOUND);
        }

        postRepository.decrementReactionCount(postId);
        return true;
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PaginationResponseDto getReactionsOfPost(PaginationRequestDto paginationRequestDto, Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)});
        }

        Integer pageNum = paginationRequestDto.getPageNum();
        Integer pageSize = paginationRequestDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Reaction> reactions = reactionRepository.findAllByPostId(postId, pageable);

        List<ReactionResponseDto> reactionList = reactions.getContent().stream()
                .map(reactionMapper::toReactionResponseDto)
                .collect(Collectors.toList());

        PagingMeta metadata = new PagingMeta();
        metadata.setPageNum(pageNum);
        metadata.setPageSize(pageSize);
        metadata.setTotalElements(reactions.getTotalElements());
        metadata.setTotalPages(reactions.getTotalPages());

        return new PaginationResponseDto<>(metadata, reactionList);

    }
}
