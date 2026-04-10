package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.response.MediaResponseDto;

import java.util.List;

public interface MediaService {

    public MediaResponseDto getMediaByPublicId(String publicId);
    public boolean deleteMedia(List<String> publicIdList);
}
