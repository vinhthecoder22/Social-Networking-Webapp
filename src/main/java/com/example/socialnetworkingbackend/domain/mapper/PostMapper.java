package com.example.projectbase.domain.mapper;

import com.example.projectbase.domain.dto.response.PostResponseDto;
import com.example.projectbase.domain.dto.response.SharePostResponseDto;
import com.example.projectbase.domain.entity.Post;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PostMapper {

    PostResponseDto toPostResponseDto(Post post);

    SharePostResponseDto toDto(Post post);

}
