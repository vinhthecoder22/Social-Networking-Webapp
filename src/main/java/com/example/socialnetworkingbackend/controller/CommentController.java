package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.request.CommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.ReplyCommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.request.UpdateCommentRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.CommentResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.CursorPageResponse;
import com.example.socialnetworkingbackend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;

@Slf4j
@RestApiV1
@RequiredArgsConstructor
@Validated
@Tag(name = "Comment", description = "CRUD API cho bình luận và trả lời bình luận (Cursor Pagination)")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Thêm bình luận gốc cho bài viết")
    @PostMapping(UrlConstant.Comment.ADD_COMMENT)
    public ResponseEntity<?> addComment(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid CommentRequestDto requestDto,
            Principal principal) {
        CommentResponseDto created = commentService.addComment(postId, requestDto, principal.getName());
        return VsResponseUtil.success(HttpStatus.CREATED, created);
    }

    @Operation(summary = "Trả lời bình luận (Reply)")
    @PostMapping(UrlConstant.Comment.REPLY_COMMENT)
    public ResponseEntity<?> replyToComment(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid ReplyCommentRequestDto requestDto,
            Principal principal) {
        CommentResponseDto reply = commentService.replyToComment(postId, requestDto, principal.getName());
        return VsResponseUtil.success(HttpStatus.CREATED, reply);
    }

    @Operation(summary = "Lấy danh sách bình luận gốc (Root Comments) bằng Cursor")
    @GetMapping(UrlConstant.Comment.GET_COMMENTS)
    public ResponseEntity<?> getCommentsByPost(
            @PathVariable("postId") Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        CursorPageResponse<CommentResponseDto> response = commentService.getCommentsByPostCursor(postId, cursor, size);
        return VsResponseUtil.success(response);
    }

    @Operation(summary = "Lấy danh sách phản hồi (Replies) của một bình luận gốc bằng Cursor")
    @GetMapping(UrlConstant.Comment.GET_REPLIES)
    public ResponseEntity<?> getRepliesOfComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long parentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size) {
        CursorPageResponse<CommentResponseDto> response = commentService.getRepliesByParentIdCursor(postId, parentId, cursor, size);
        return VsResponseUtil.success(response);
    }

    @Operation(summary = "Cập nhật bình luận")
    @PutMapping(UrlConstant.Comment.UPDATE_COMMENT)
    public ResponseEntity<?> updateComment(
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody @Valid UpdateCommentRequestDto requestDto,
            Principal principal) {

        CommentResponseDto updated = commentService.updateComment(
                commentId,
                requestDto.getContent(),
                postId,
                principal.getName()
        );
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