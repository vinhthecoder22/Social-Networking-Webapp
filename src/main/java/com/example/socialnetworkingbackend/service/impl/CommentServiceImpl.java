package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.NotificationType;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.dto.request.CommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.ReplyCommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.CommentResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Comment;
import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.CommentMapper;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.exception.UnauthorizedException;
import com.example.socialnetworkingbackend.repository.CommentRepository;
import com.example.socialnetworkingbackend.repository.PostRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    @PreAuthorize("#username == authentication.principal.username")
    @Override
    @Transactional
    public CommentResponseDto addComment(Long postId, CommentRequestDto requestDto, String username) {
        // Fetch Post lên 1 lần duy nhất (Validate + lấy User bắn thông báo)
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)})
        );

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .commentLevel(0)
                .post(post)
                .user(user)
                .build();

        Comment savedComment = commentRepository.save(comment);

        postRepository.incrementCommentCount(postId);

        if (!post.getUser().getUsername().equals(username)) {
            String message = user.getFirstName() + " đã bình luận về bài viết của bạn.";
            notificationService.sendNotification(
                    post.getUser(),
                    user,
                    NotificationType.COMMENT,
                    String.valueOf(postId),
                    message
            );
        }

        return commentMapper.toCommentResponseDto(savedComment);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public CommentResponseDto replyToComment(Long postId, ReplyCommentRequestDto requestDto, String username) {
        Long parentCommentId = requestDto.getParentCommentId();
        log.info("Adding reply to comment {} on post {} by user {}", parentCommentId, postId, username);

        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));

        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Comment.ERR_PARENT_COMMENT_NOT_FOUND, new String[]{String.valueOf(parentCommentId)}));

        Post post = parent.getPost();
        if (!post.getId().equals(postId)) {
            throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST, new String[]{String.valueOf(parentCommentId), String.valueOf(postId)});
        }

        Comment reply = Comment.builder()
                .content(requestDto.getContent())
                .commentLevel(parent.getCommentLevel() + 1)
                .post(post)
                .user(user)
                .parent(parent)
                .build();

        Comment savedReply = commentRepository.save(reply);
        postRepository.incrementCommentCount(postId);

        if (!parent.getUser().getUsername().equals(username)) {
            String message = user.getFirstName() + " đã phản hồi bình luận của bạn.";
            notificationService.sendNotification(
                    parent.getUser(),
                    user,
                    NotificationType.COMMENT,
                    String.valueOf(postId),
                    message
            );
        }

        return commentMapper.toCommentResponseDto(savedReply);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public CommentResponseDto updateComment(Long commentId, String content, Long postId, String username) {
        log.debug("Updating comment {} on post {} by user {}", commentId, postId, username);

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_ID, new String[]{String.valueOf(commentId)})
        );

        validateCommentOwnership(comment, postId, username);

        comment.setContent(content);

        log.debug("Comment {} updated successfully", commentId);
        return commentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {
        log.debug("Deleting comment {} on post {} by user {}", commentId, postId, username);

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_ID, new String[]{String.valueOf(commentId)})
        );

        validateCommentOwnership(comment, postId, username);

        // Đếm tổng số comment sẽ bị xóa (Gốc + toàn bộ các nhánh con/cháu)
        int totalDeleted = calculateTotalCommentsToDelete(comment);

        // Xóa comment (Hibernate sẽ tự động xóa các replies nhờ Cascade)
        commentRepository.delete(comment);

        // Trừ đúng số lượng đã xóa
        postRepository.decrementCommentCountBy(postId, totalDeleted);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findParentCommentsByPostId(postId, pageable)
                .map(commentMapper::toCommentResponseDto);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getRepliesByParentId(Long postId, Long parentId) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID, new String[]{String.valueOf(postId)});
        }
        log.debug("Getting replies for parent comment {}", parentId);
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId)
                .stream()
                .map(commentMapper::toCommentResponseDto)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public CommentResponseDto getCommentWithReplies(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_ID, new String[]{String.valueOf(commentId)})
        );
        return commentMapper.toCommentResponseDto(comment);
    }

    private int calculateTotalCommentsToDelete(Comment comment) {
        long descendantCount = commentRepository.countByParentId(comment.getId());
        return 1 + (int) descendantCount;
    }

    private void validateCommentOwnership(Comment comment, Long postId, String username) {
        // Kiểm tra xem user hiện tại có quyền ADMIN trong SecurityContext không
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(RoleConstant.ADMIN));

        if (!isAdmin) {
            if (!comment.getPost().getId().equals(postId)) {
                throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST, new String[]{String.valueOf(comment.getId())});
            }
            if (!comment.getUser().getUsername().equals(username)) {
                throw new UnauthorizedException(ErrorMessage.Comment.ERR_NOT_HAVE_PERMISSION);
            }
        }
    }
}