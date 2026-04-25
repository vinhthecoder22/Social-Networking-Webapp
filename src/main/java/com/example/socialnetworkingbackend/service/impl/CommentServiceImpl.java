package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.NotificationType;
import com.example.socialnetworkingbackend.constant.RoleConstant;
import com.example.socialnetworkingbackend.domain.dto.request.CommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.ReplyCommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.CommentResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.CursorPageResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
                .replyCount(0)
                .build();

        Comment savedComment = commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        if (!post.getUser().getUsername().equals(username)) {
            notificationService.sendNotification(
                    post.getUser(), user, NotificationType.COMMENT,
                    String.valueOf(postId), user.getFirstName() + " đã bình luận bài viết của bạn."
            );
        }

        return commentMapper.toCommentResponseDto(savedComment);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public CommentResponseDto replyToComment(Long postId, ReplyCommentRequestDto requestDto, String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{username}));

        Comment parentOrTarget = commentRepository.findById(requestDto.getParentCommentId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Comment.ERR_PARENT_COMMENT_NOT_FOUND));

        if (!parentOrTarget.getPost().getId().equals(postId)) {
            throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST);
        }

        // Ép về 2 level: Xác định Root Comment và User đang bị tag
        Comment rootParent = (parentOrTarget.getParent() != null) ? parentOrTarget.getParent() : parentOrTarget;
        User replyToUser = (parentOrTarget.getParent() != null) ? parentOrTarget.getUser() : null;

        Comment reply = Comment.builder()
                .content(requestDto.getContent())
                .commentLevel(1)
                .post(parentOrTarget.getPost())
                .user(user)
                .parent(rootParent)
                .replyToUser(replyToUser)
                .replyCount(0)
                .build();

        Comment savedReply = commentRepository.save(reply);

        // Tăng count ở Root Comment và Post
        commentRepository.updateReplyCount(rootParent.getId(), 1);
        postRepository.incrementCommentCount(postId);

        if (!parentOrTarget.getUser().getUsername().equals(username)) {
            notificationService.sendNotification(
                    parentOrTarget.getUser(), user, NotificationType.COMMENT,
                    String.valueOf(postId), user.getFirstName() + " đã phản hồi bình luận của bạn."
            );
        }

        return commentMapper.toCommentResponseDto(savedReply);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public CommentResponseDto updateComment(Long commentId, String content, Long postId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_ID));

        validateCommentOwnership(comment, postId, username);
        comment.setContent(content);
        return commentMapper.toCommentResponseDto(commentRepository.save(comment));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_ID));

        validateCommentOwnership(comment, postId, username);

        int totalDeleted = 1;

        if (comment.getParent() == null) {
            // Nếu xóa Root Comment -> Số comment trên bài viết giảm = 1 (chính nó) + toàn bộ số replies
            totalDeleted += comment.getReplyCount();
        } else {
            // Nếu xóa Reply -> Trừ đi 1 ở Root Comment
            commentRepository.updateReplyCount(comment.getParent().getId(), -1);
        }

        commentRepository.delete(comment);
        postRepository.decrementCommentCountBy(postId, totalDeleted);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponseDto> getCommentsByPostCursor(Long postId, Long cursor, int size) {
        // Lấy dư 1 phần tử để kiểm tra hasNext
        PageRequest pageRequest = PageRequest.of(0, size + 1);
        List<Comment> comments = commentRepository.findParentCommentsWithCursor(postId, cursor, pageRequest);

        return buildCursorResponse(comments, size);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponseDto> getRepliesByParentIdCursor(Long postId, Long parentId, Long cursor, int size) {
        if (!postRepository.existsById(postId)) {
            throw new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID);
        }

        PageRequest pageRequest = PageRequest.of(0, size + 1);
        List<Comment> comments = commentRepository.findRepliesWithCursor(parentId, cursor, pageRequest);

        return buildCursorResponse(comments, size);
    }

    // --- Private Helper Methods ---

    private CursorPageResponse<CommentResponseDto> buildCursorResponse(List<Comment> comments, int size) {
        boolean hasNext = comments.size() > size;
        Long nextCursor = null;

        if (hasNext) {
            comments.remove(comments.size() - 1);
        }

        if (!comments.isEmpty()) {
            nextCursor = comments.get(comments.size() - 1).getId();
        }

        List<CommentResponseDto> data = comments.stream()
                .map(commentMapper::toCommentResponseDto)
                .collect(Collectors.toList());

        return CursorPageResponse.<CommentResponseDto>builder()
                .data(data)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private void validateCommentOwnership(Comment comment, Long postId, String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(RoleConstant.ADMIN));

        if (!isAdmin) {
            if (!comment.getPost().getId().equals(postId)) {
                throw new NotFoundException(ErrorMessage.Comment.ERR_NOT_FOUND_COMMENT_IN_POST);
            }
            if (!comment.getUser().getUsername().equals(username)) {
                throw new UnauthorizedException(ErrorMessage.Comment.ERR_NOT_HAVE_PERMISSION);
            }
        }
    }
}