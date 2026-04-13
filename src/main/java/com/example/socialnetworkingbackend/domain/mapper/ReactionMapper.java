package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.response.ReactionResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Reaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReactionMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user.id", target = "userId")
    ReactionResponseDto toReactionResponseDto(Reaction reaction);

}
