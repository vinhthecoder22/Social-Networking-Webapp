package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.response.CommentResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserSummaryDto;
import com.example.socialnetworkingbackend.domain.entity.Comment;
import com.example.socialnetworkingbackend.domain.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    List<CommentResponseDto> toCommentResponseDtoList(List<Comment> comments);

    UserSummaryDto toUserSummaryDto(User user);

    default CommentResponseDto toCommentResponseDto(Comment comment) {
        if (comment == null) return null;

        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCommentLevel(comment.getCommentLevel());
        dto.setAuthor(toUserSummaryDto(comment.getUser()));

        if (comment.getReplyToUser() != null) {
            dto.setReplyToUser(toUserSummaryDto(comment.getReplyToUser()));
        }

        dto.setReplyCount(comment.getReplyCount() != null ? comment.getReplyCount() : 0);
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setLastModifiedAt(comment.getLastModifiedAt());
        dto.setParentCommentId(comment.getParent() == null ? null : comment.getParent().getId());
        dto.setPostId(comment.getPost().getId());

        return dto;
    }
}