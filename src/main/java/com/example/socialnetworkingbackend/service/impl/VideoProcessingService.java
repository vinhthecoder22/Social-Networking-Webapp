package com.example.projectbase.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.projectbase.constant.UploadStatusConstant;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.mapper.MediaMapper;
import com.example.projectbase.exception.BadRequestException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.UserRepository;
import com.example.projectbase.util.MediaProcessingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
@RequiredArgsConstructor
public class VideoProcessingService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    private final Cloudinary cloudinary;
    private final UserRepository userRepository;

    @Async("rawVideoExecutor")
    public CompletableFuture<MediaResponseDto> uploadVideo(File videoFile, String contentTypeFile, String userId) {
        try {
            MediaProcessingUtil.validateFile(videoFile, contentTypeFile);
            Media mediaPending = MediaProcessingUtil.createMediaPending(videoFile, "video", userId, null,
                    mediaRepository, userRepository);

            return compressVideo(videoFile, mediaPending)
                    .thenApply(compressFile -> {
                        log.info("starting upload to cloudinary");
                        MediaResponseDto responseDto = uploadVideoToCloudinary(compressFile, mediaPending);
                        return responseDto;
                    })
                    .exceptionally(ex -> {
                        log.info("Have some error when uploading: {}", ex.getCause());
                        updateMediaStatusAsync(mediaPending, UploadStatusConstant.ERROR);
                        throw new CompletionException(ex);
                    });
        } catch (Exception e) {
            log.info("Have some exception in method upload Video: {}", e.getMessage());
            return CompletableFuture.failedFuture(e);
        }

    }

    @Async("rawVideoExecutor")
    public CompletableFuture<File> compressVideo(File originalFile, Media media) {
        log.info("Compressing video");
        File compressedFile = null;

        try {
            compressedFile = File.createTempFile("compressed_" + System.currentTimeMillis(), ".mp4");

            if (compressedFile.exists()) {
                compressedFile.delete();
            }
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", originalFile.getAbsolutePath(),
                    "-r", "30",
                    "-c:v", "libx264",
                    "-crf", "23",
                    "-preset", "medium",
                    "-vf", "scale='min(1080,iw)':-2",
                    "-c:a", "aac",
                    "-b:a", "192k",
                    "-movflags", "+faststart",
                    compressedFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[FFmpeg] {}", line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finished = process.waitFor(180, TimeUnit.SECONDS);
            if (!finished) {
                log.warn("[FFmpeg] Video process timed out. Destroying process...");
                process.destroyForcibly();
                updateMediaStatusAsync(media, UploadStatusConstant.ERROR);
                throw new BadRequestException("Video process timed out.");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("[FFmpeg failed with exit code");
                updateMediaStatusAsync(media, UploadStatusConstant.ERROR);
                throw new BadRequestException("FFmpeg failed with exit code " + exitCode);
            }

            updateMediaStatusAsync(media, UploadStatusConstant.PROCESSING);
            log.info("Compressed video successfully");
            return CompletableFuture.completedFuture(compressedFile);

        } catch (Exception e) {
            log.info("error in catch {}", e.getMessage());
            if (compressedFile != null && compressedFile.exists()) {
                compressedFile.delete();
            }
            updateMediaStatusAsync(media, UploadStatusConstant.ERROR);
            throw new RuntimeException(e);

        } finally {

            if (originalFile != null && originalFile.exists()) {
                originalFile.delete();
            }
        }

    }

    private MediaResponseDto uploadVideoToCloudinary(File compressedFile, Media videoUpload) {
        log.info("Uploading video to cloudinary");
        try {
            Map<String, Object> metaData = ObjectUtils.asMap(
                    "resource_type", "video",
                    "quality", "auto",
                    "video_codec", "h264",
                    "chunk_size", 7000000,
                    "eager_async", true,
                    "public_id", videoUpload.getPublicId(),
                    "invalidate", true,
                    "eager", Arrays.asList(
                            new Transformation()
                                    .fetchFormat("m3u8"),
                            new Transformation()
                                    .startOffset("auto")
                                    .width(720)
                                    .height(1080)
                                    .crop("fill")
                                    .gravity("center")
                                    .fetchFormat("jpg")));
            log.info("start uploading video to cloudinary");
            Map<String, Object> result = cloudinary.uploader().uploadLarge(compressedFile, metaData);
            List<Map<String, Object>> eagerList = (List<Map<String, Object>>) result.get("eager");
            if (eagerList != null) {
                for (Map<String, Object> eager : eagerList) {
                    String url = (String) eager.get("secure_url");
                    if (url != null && url.endsWith(".jpg")) {
                        videoUpload.setThumbnailUrl(url);
                    } else if (url != null && url.endsWith(".m3u8")) {
                        videoUpload.setPlaybackUrl(url);
                    }
                }
            }

            videoUpload.setSecureUrl(result.get("secure_url").toString());
            videoUpload.setHeight(Long.parseLong(result.get("height").toString()));
            videoUpload.setWidth(Long.parseLong(result.get("width").toString()));
            videoUpload.setFormat(result.get("format").toString());
            videoUpload.setStatus(UploadStatusConstant.DONE);

            MediaResponseDto mediaResponseDto = mediaMapper.toMediaResponseDto(mediaRepository.save(videoUpload));
            mediaResponseDto.setAuthorId(videoUpload.getUser().getId());
            log.info("Uploaded video to cloudinary successfully");
            return mediaResponseDto;
        } catch (Exception e) {
            log.info("have been error in catch cloudinary {}", e.getMessage());
            updateMediaStatusAsync(videoUpload, UploadStatusConstant.ERROR);
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

    @Async("rawVideoExecutor")
    public void updateMediaStatusAsync(Media media, UploadStatusConstant status) {
        try {
            media.setStatus(status);
            mediaRepository.save(media);
        } catch (Exception e) {
            log.error("Failed to update media status: {}", e.getMessage());
        }
    }

}
