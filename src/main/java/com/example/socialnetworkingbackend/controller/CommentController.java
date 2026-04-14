package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.request.CommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.ReplyCommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.CommentResponseDto;
import com.example.socialnetworkingbackend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RestApiV1
@RequiredArgsConstructor
@Validated
@Tag(name = "Comment", description = "CRUD API cho bình luận và trả lời bình luận")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Thêm bình luận cho bài viết")
    @PostMapping(UrlConstant.Comment.ADD_COMMENT)
    public ResponseEntity<?> addComment(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid CommentRequestDto requestDto,
            Principal principal) {
        CommentResponseDto created = commentService.addComment(postId, requestDto, principal.getName());
        return VsResponseUtil.success(HttpStatus.CREATED, created);
    }

    @Operation(summary = "Trả lời bình luận")
    @PostMapping(UrlConstant.Comment.REPLY_COMMENT)
    public ResponseEntity<?> replyToComment(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid ReplyCommentRequestDto requestDto,
            Principal principal) {
        CommentResponseDto reply = commentService.replyToComment(postId, requestDto, principal.getName());
        return VsResponseUtil.success(HttpStatus.CREATED, reply);
    }

    @Operation(summary = "Lấy bình luận gốc và trả lời theo bài viết (lazy loading, phân trang)")
    @GetMapping(UrlConstant.Comment.GET_COMMENTS)
    public ResponseEntity<?> getCommentsByPost(
            @PathVariable("postId") Long postId,
            Pageable pageable) {
        Page<CommentResponseDto> page = commentService.getCommentsByPost(postId, pageable);
        return VsResponseUtil.success(page);
    }

    @Operation(summary = "Lấy bình luận gốc và các phản hồi")
    @GetMapping(UrlConstant.Comment.GET_COMMENT_WITH_REPLIES)
    public ResponseEntity<?> getCommentWithReplies(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId) {
        CommentResponseDto responseDto = commentService.getCommentWithReplies(postId, commentId);
        return VsResponseUtil.success(responseDto);
    }

    @Operation(summary = "Lấy các phản hồi (replies) của một bình luận gốc")
    @GetMapping(UrlConstant.Comment.GET_REPLIES)
    public ResponseEntity<?> getRepliesOfComment(
            @PathVariable("postId") Long postId,
            @PathVariable Long commentId) {
        List<CommentResponseDto> replies = commentService.getRepliesByParentId(postId, commentId);
        return VsResponseUtil.success(replies);
    }

    @Operation(summary = "Cập nhật bình luận")
    @PutMapping(UrlConstant.Comment.UPDATE_COMMENT)
    public ResponseEntity<?> updateComment(@PathVariable("commentId") Long commentId,
                                           @RequestParam("content") String content,
                                           @PathVariable("postId") Long postId,
                                           Principal principal) {
        CommentResponseDto updated = commentService.updateComment(commentId, content, postId, principal.getName());
        return VsResponseUtil.success(updated);
    }

    @Operation(summary = "Xóa bình luận")
    @DeleteMapping(UrlConstant.Comment.DELETE_COMMENT)
    public ResponseEntity<?> deleteComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            Principal principal) {
        commentService.deleteComment(postId, commentId, principal.getName());
        return VsResponseUtil.success(HttpStatus.NO_CONTENT);
    }

}
