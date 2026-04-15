package com.example.socialnetworkingbackend.service;

import com.example.socialnetworkingbackend.domain.dto.request.SharePostRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.SharePostResponseDto;

public interface ShareService {

    public SharePostResponseDto sharePost(Long postId, SharePostRequestDto request);

}
