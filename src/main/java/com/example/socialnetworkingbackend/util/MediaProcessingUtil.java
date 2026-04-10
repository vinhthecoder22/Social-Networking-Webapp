package com.example.socialnetworkingbackend.util;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.MediaConstant;
import com.example.socialnetworkingbackend.constant.UploadStatusConstant;
import com.example.socialnetworkingbackend.domain.entity.Media;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.exception.InternalServerException;
import com.example.socialnetworkingbackend.exception.InvalidException;
import com.example.socialnetworkingbackend.exception.MaxUploadSizeMediaException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.MediaRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;

import lombok.extern.log4j.Log4j2;
import org.springframework.lang.Nullable;

import java.io.File;

@Log4j2
public class MediaProcessingUtil {


    public static void validateFile(File file, String contentType) {
        log.info("validateFile: {}", contentType);
        String formatFile = contentType;
        if (formatFile == null || (!formatFile.startsWith("video/") && !formatFile.startsWith("image/") && !formatFile.startsWith("audio/"))) {
            throw new InvalidException(ErrorMessage.Media.ERR_INVALID_MEDIA_TYPE);
        }
        if ((formatFile.startsWith("image/") && file.length() > MediaConstant.MAX_SIZE_IMAGE)) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_IMAGE);
        }
        if ((formatFile.startsWith("video/") && file.length() > MediaConstant.MAX_SIZE_VIDEO)) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_VIDEO);
        }
        if ((formatFile.startsWith("audio/") && file.length() > MediaConstant.MAX_SIZE_AUDIO)) {
            throw new MaxUploadSizeMediaException(ErrorMessage.Media.ERR_MAX_SIZE_UPLOAD_AUDIO);
        }
        log.info("The file is valid");
    }

    public static String generatePublicIdMedia(File file, String typeMedia) {
        if (typeMedia.equals("video")) {
            return "video" + System.currentTimeMillis() + file.getName();
        } else if (typeMedia.equals("image")) {
            return "image" + System.currentTimeMillis() + file.getName();
        }
        return "audio" + System.currentTimeMillis() + file.getName();
    }

    public static Media createMediaPending(File multipartFile,
                                    String typeMedia,
                                    String userId,
                                    @Nullable String singerName,
                                    MediaRepository mediaRepository,
                                    UserRepository userRepository) {
        try {
            log.info("Creating media pending for type: {}", typeMedia);

            log.info("Successfully get user principal");
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
            log.info("current user: {}", user.toString());
            String publicId = generatePublicIdMedia(multipartFile, typeMedia);
            log.info("Generated publicId: {}", publicId);
            Media media = Media.builder()
                    .publicId(publicId)
                    .resourceType(typeMedia)
                    .dataSize(multipartFile.length())
                    .status(UploadStatusConstant.PENDING)
                    .user(user)
                    .singerName(singerName)
                    .build();
            log.info("Created media pending successfully");
            return mediaRepository.save(media);
        } catch (Exception e) {
            log.error("Error while creating media pending", e);
            throw new InternalServerException(ErrorMessage.ERR_EXCEPTION_GENERAL);
        }
        
    }

    public static void cleanupFiles(File... files) {
        for (File file : files) {
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    log.warn("Failed to delete file: {}", file.getAbsolutePath());
                }
            }
        }
    }

}
