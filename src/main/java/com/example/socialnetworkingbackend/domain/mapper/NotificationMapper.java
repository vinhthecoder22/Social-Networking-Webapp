package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.response.NotificationResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface NotificationMapper {

    @Mapping(target = "message", ignore = true)
    NotificationResponseDto toNotificationResponseDto(Notification notification);

}