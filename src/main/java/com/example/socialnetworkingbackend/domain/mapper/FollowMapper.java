package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.response.FollowResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Follow;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    FollowResponseDto toFollowResponseDto(Follow follow);
}
