package com.example.socialnetworkingbackend.domain.mapper;

import com.example.socialnetworkingbackend.domain.dto.response.NotificationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.response.UserSummaryDto;
import com.example.socialnetworkingbackend.domain.entity.Notification;
import com.example.socialnetworkingbackend.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponseDto toNotificationResponseDto(Notification notification);

    UserSummaryDto toUserSummaryDto(User user);

}