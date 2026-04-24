package com.example.socialnetworkingbackend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.socialnetworkingbackend.constant.UploadStatusConstant;
import com.example.socialnetworkingbackend.domain.dto.response.MediaResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Media;
import com.example.socialnetworkingbackend.domain.mapper.MediaMapper;
import com.example.socialnetworkingbackend.repository.MediaRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.util.MediaProcessingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ImageProcessingService {

    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final MediaMapper mediaMapper;

    @Async("rawImageExecutor")
    public CompletableFuture<MediaResponseDto> uploadImage(File imageFile, String contentTypeFile, String userId) {
        try {
            MediaProcessingUtil.validateFile(imageFile, contentTypeFile);
            Media imagePending = MediaProcessingUtil.createMediaPending(imageFile, "image", userId, null,
                    mediaRepository, userRepository);

            MediaResponseDto responseDto = uploadImageToCloudinary(imageFile, imagePending);

            return CompletableFuture.completedFuture(responseDto);

        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }

    }

    @Async("rawImageExecutor")
    public CompletableFuture<List<MediaResponseDto>> uploadMultipleImages(List<File> imageFiles,
            List<String> contentTypeFileList, String userId) {

        List<CompletableFuture<MediaResponseDto>> futures = new ArrayList<>();

        for (int i = 0; i < imageFiles.size(); i++) {
            File imageFile = imageFiles.get(i);
            String contentType = contentTypeFileList != null && i < contentTypeFileList.size()
                    ? contentTypeFileList.get(i)
                    : null;

            futures.add(uploadImage(imageFile, contentType, userId));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    private MediaResponseDto uploadImageToCloudinary(File imageFile, Media imageUpload) {
        log.info("Uploading image to cloudinary");
        try {
            updateMediaStatusAsync(imageUpload, UploadStatusConstant.PROCESSING);
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "image",
                    "quality", "auto",
                    "fetch_format", "auto",
                    "public_id", imageUpload.getPublicId());
            log.info("start uploading image to cloudinary");
            Map<String, Object> result = cloudinary.uploader().upload(imageFile, metaData);

            imageUpload.setDataSize(imageFile.length());
            imageUpload.setFormat(result.get("format").toString());
            imageUpload.setPublicId(result.get("public_id").toString());
            imageUpload.setSecureUrl(result.get("secure_url").toString());
            imageUpload.setResourceType(result.get("resource_type").toString());
            imageUpload.setWidth(Long.valueOf((Integer) result.get("width")));
            imageUpload.setHeight(Long.valueOf((Integer) result.get("height")));
            imageUpload.setStatus(UploadStatusConstant.DONE);

            MediaResponseDto responseDto = mediaMapper.toMediaResponseDto(mediaRepository.save(imageUpload));

            log.info("Uploaded image to cloudinary successfully");
            return responseDto;
        } catch (Exception e) {
            log.info("have been error in method uploadImageToCloudinary: {}", e.getMessage());
            updateMediaStatusAsync(imageUpload, UploadStatusConstant.ERROR);
            if (imageFile != null && imageFile.exists()) {
                imageFile.delete();
            }
            throw new RuntimeException(e);
        } finally {
            if (imageFile != null && imageFile.exists()) {
                imageFile.delete();
            }
        }
    }

    public void updateMediaStatusAsync(Media media, UploadStatusConstant status) {
        try {
            media.setStatus(status);
            mediaRepository.save(media);
        } catch (Exception e) {
            log.error("Failed to update media status: {}", e.getMessage());
        }
    }
}
