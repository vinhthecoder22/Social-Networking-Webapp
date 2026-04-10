package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.response.PostResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, MediaMapper.class})
public interface PostMapper {

    @Mapping(source = "user", target = "createdBy")
    @Mapping(source = "originalPost.id", target = "originalPostId")
    PostResponseDto toPostResponseDto(Post post);

}