package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.response.MediaResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MediaMapper {

    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "user", target = "author")
    MediaResponseDto toMediaResponseDto(Media media);
}