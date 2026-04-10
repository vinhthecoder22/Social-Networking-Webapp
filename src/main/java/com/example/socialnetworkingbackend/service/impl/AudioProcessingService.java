package com.example.socialnetworkingbackend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.socialnetworkingbackend.constant.UploadStatusConstant;
import com.example.socialnetworkingbackend.domain.dto.response.MediaResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Media;
import com.example.socialnetworkingbackend.domain.mapper.MediaMapper;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.repository.MediaRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.util.MediaProcessingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class AudioProcessingService {

    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final MediaMapper mediaMapper;
    private final Cloudinary cloudinary;
    private final ImageProcessingService imageProcessingService;

    @Async("rawAudioExecutor")
    public CompletableFuture<List<MediaResponseDto>> uploadAudio(File audioFile, File thumbnailFile,
            List<String> contentTypeFileList, String singerName, String userId) {
        try {
            MediaProcessingUtil.validateFile(audioFile, contentTypeFileList.get(0));
            Media audioPending = MediaProcessingUtil.createMediaPending(audioFile, "audio", userId, singerName,
                    mediaRepository, userRepository);

            return compressAudioAsync(audioFile, audioPending).thenApply(compressedFile -> {
                try {
                    List<MediaResponseDto> responseDtoList = uploadAudioToCloudinary(compressedFile, thumbnailFile,
                            contentTypeFileList.get(1), audioPending, userId);
                    return responseDtoList;
                } catch (ExecutionException e) {
                    log.info("Error executor in method audioUpload: {}", e.getCause());
                    updateMediaStatusAsync(audioPending, UploadStatusConstant.ERROR);
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    log.info("Error interrupted in method audioUpload: {}", e.getCause());
                    updateMediaStatusAsync(audioPending, UploadStatusConstant.ERROR);
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async("rawAudioExecutor")
    public CompletableFuture<File> compressAudioAsync(File audioFile, Media media) {
        File compressFile = compressAudio(audioFile, media);
        return CompletableFuture.completedFuture(compressFile);
    }

    private File compressAudio(File audioFile, Media media) {

        File compressedFile = null;

        try {

            compressedFile = File.createTempFile("compressed_", ".m4a");

            if (compressedFile.exists()) {
                compressedFile.delete();
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", audioFile.getAbsolutePath(),
                    "-vn",
                    "-c:a", "aac",
                    "-b:a", "192k",
                    compressedFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line);
                }
            }

            boolean finished = process.waitFor(150, TimeUnit.SECONDS);

            if (!finished) {
                log.warn("[FFmpeg] Video process timed out. Destroying process...");
                process.destroyForcibly();
                media.setStatus(UploadStatusConstant.ERROR);
                mediaRepository.save(media);
                throw new BadRequestException("Video process timed out.");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                media.setStatus(UploadStatusConstant.ERROR);
                mediaRepository.save(media);
                throw new BadRequestException("FFmpeg failed with exit code " + exitCode);
            }
            updateMediaStatusAsync(media, UploadStatusConstant.DONE);
            return compressedFile;
        } catch (Exception e) {
            if (compressedFile != null && compressedFile.exists()) {
                compressedFile.delete();
            }
            updateMediaStatusAsync(media, UploadStatusConstant.ERROR);
            throw new RuntimeException(e);
        } finally {
            if (audioFile.exists() && audioFile != null) {
                audioFile.delete();
            }
        }
    }

    private List<MediaResponseDto> uploadAudioToCloudinary(File compressedFile, File thumbnailFile,
            String contentTypeFile, Media audioUpload, String userId) throws ExecutionException, InterruptedException {

        log.info("Uploading audio to cloudinary");
        List<MediaResponseDto> responseDtoList = new ArrayList<>();
        CompletableFuture<MediaResponseDto> uploadThumbnail = imageProcessingService.uploadImage(thumbnailFile,
                contentTypeFile, userId);
        MediaResponseDto thumbnailResult = uploadThumbnail.get();
        responseDtoList.add(thumbnailResult);
        try {
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "video",
                    "quality", "auto",
                    "chunk_size", 7000000,
                    "eager_async", true,
                    "public_id", audioUpload.getPublicId(),
                    "invalidate", true,
                    "eager", Arrays.asList(
                            new Transformation()
                                    .overlay(thumbnailResult.getPublicId())
                                    .crop("fill")
                                    .width("300")
                                    .height("300")));
            log.info("start uploading audio to cloudinary");
            Map<String, Object> result = cloudinary.uploader().uploadLarge(compressedFile, metaData);
            log.info("result: {}", result);

            audioUpload.setDataSize(Long.valueOf((Integer) result.get("bytes")));
            audioUpload.setFormat(result.get("format").toString());
            audioUpload.setSecureUrl(result.get("secure_url").toString());
            audioUpload.setStatus(UploadStatusConstant.DONE);

            Media savedMedia = mediaRepository.save(audioUpload);
            MediaResponseDto audioResponseDto = mediaMapper.toMediaResponseDto(savedMedia);
            responseDtoList.add(audioResponseDto);

            log.info("Uploaded audio to cloudinary successfully");
            return responseDtoList;
        } catch (Exception e) {
            log.info("have been error in method uploadAudioToCloudinary: {}", e.getMessage());
            updateMediaStatusAsync(audioUpload, UploadStatusConstant.ERROR);
            if (compressedFile != null && compressedFile.exists()) {
                compressedFile.delete();
            }
            throw new RuntimeException(e);
        } finally {
            if (compressedFile != null && compressedFile.exists()) {
                compressedFile.delete();
            }
        }
    }

    @Async("rawAudioExecutor")
    public void updateMediaStatusAsync(Media media, UploadStatusConstant status) {
        try {
            media.setStatus(status);
            mediaRepository.save(media);
        } catch (Exception e) {
            log.error("Failed to update media status: {}", e.getMessage());
        }
    }
}
