package com.example.socialnetworkingbackend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.domain.dto.response.MediaResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Media;
import com.example.socialnetworkingbackend.domain.mapper.MediaMapper;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.MediaRepository;
import com.example.socialnetworkingbackend.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;


    @Override
    public MediaResponseDto getMediaByPublicId(String publicId) {
        Media media = mediaRepository.findMediaByPublicId(publicId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{publicId}));

        return mediaMapper.toMediaResponseDto(media);
    }

    @Transactional
    @Override
    public boolean deleteMedia(List<String> publicIdList) {
        List<Media> mediaList = mediaRepository.findAllByPublicIdIn(publicIdList);
        log.info("mediaList: {}", mediaList.toString());

        List<String> invalidPublicId = new ArrayList<>();
        for (String publicId : publicIdList) {
            if (mediaList.stream().noneMatch(media -> media.getPublicId().equals(publicId))) {
                invalidPublicId.add(publicId);
            }
        }

        if (!invalidPublicId.isEmpty()) {
            throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[]{String.valueOf(invalidPublicId)});
        }

        // Xóa dưới DB trước
        mediaRepository.deleteAllByPublicIdIn(publicIdList);

        // Xóa trên Cloudinary sau
        for (Media media : mediaList) {
            try {
                Map<String, Object> metaData = ObjectUtils.asMap(
                        "resource_type", media.getResourceType(),
                        "invalidated", true
                );
                cloudinary.uploader().destroy(media.getPublicId(), metaData);

                if ("audio".equals(media.getResourceType())) {
                    Map<String, Object> metaDataThumbnail = ObjectUtils.asMap(
                            "resource_type", "image",
                            "invalidated", true
                    );
                    cloudinary.uploader().destroy(media.getPublicId(), metaDataThumbnail);
                }
            } catch (Exception e) {
                log.error("Failed to delete media on Cloudinary: {}", media.getPublicId(), e);
                throw new RuntimeException("Failed to delete media on Cloudinary, transaction rolled back");
            }
        }
        return true;
    }
}