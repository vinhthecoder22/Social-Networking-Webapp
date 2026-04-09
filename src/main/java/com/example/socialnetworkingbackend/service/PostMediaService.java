package com.example.projectbase.service.impl;

import com.example.projectbase.constant.ErrorMessage;
import com.example.projectbase.constant.MediaType;
import com.example.projectbase.constant.PostStatusConstant;
import com.example.projectbase.domain.dto.response.MediaResponseDto;
import com.example.projectbase.domain.entity.Media;
import com.example.projectbase.domain.entity.Post;
import com.example.projectbase.exception.NotFoundException;
import com.example.projectbase.repository.MediaRepository;
import com.example.projectbase.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostMediaService {

   private final PostRepository postRepository;
   private final MediaRepository mediaRepository;
   private final VideoProcessingService videoProcessingService;
   private final AudioProcessingService audioProcessingService;
   private final ImageProcessingService imageProcessingService;

   @Async("taskExecutor")
   public void processPostMedia(Long postId, List<String> filePaths, List<String> contentTypeList, String singerName) {
      log.info("Starting async media processing for postId: {}", postId);
      List<File> filesToProcess = new ArrayList<>();
      try {
         Post post = postRepository.findById(postId)
               .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID,
                     new String[] { String.valueOf(postId) }));

         post.setStatus(PostStatusConstant.PROCESSING);
         postRepository.save(post);

         for (String path : filePaths) {
            File file = new File(path);
            if (file.exists()) {
               filesToProcess.add(file);
            } else {
               log.error("File not found at path: {}", path);
            }
         }

         if (filesToProcess.isEmpty()) {
            log.error("No files found to process for postId: {}", postId);
            post.setStatus(PostStatusConstant.REJECTED);
            postRepository.save(post);
            return;
         }

         processAndSaveMedia(post, filesToProcess, contentTypeList, singerName, post.getUser().getId());

      } catch (Exception e) {
         log.error("Error processing media for postId: {}", postId, e);
         // Optionally set post status to FAILED here if initial setup fails
      }
   }

   private void processAndSaveMedia(Post post, List<File> files, List<String> contentTypeFileList, String singerName,
         String userId) {
      MediaType mediaType = post.getMediaType();
      CompletableFuture<Void> processingFuture;

      switch (mediaType) {
         case IMAGE:
            processingFuture = imageProcessingService.uploadMultipleImages(files, contentTypeFileList, userId)
                  .thenAccept(dtos -> dtos.forEach(dto -> saveMediaToPost(post, dto)));
            break;
         case VIDEO:
            processingFuture = videoProcessingService.uploadVideo(files.get(0), contentTypeFileList.get(0), userId)
                  .thenAccept(dto -> saveMediaToPost(post, dto));
            break;
         case AUDIO:
            processingFuture = audioProcessingService
                  .uploadAudio(files.get(0), files.get(1), contentTypeFileList, singerName, userId)
                  .thenAccept(dtos -> dtos.forEach(dto -> saveMediaToPost(post, dto)));
            break;
         default:
            processingFuture = CompletableFuture.completedFuture(null);
      }

      processingFuture.whenComplete((result, ex) -> {
         if (ex != null) {
            log.error("Media processing failed for postId: {}", post.getId(), ex);
            post.setStatus(PostStatusConstant.REJECTED);
         } else {
            log.info("Media processing success for postId: {}", post.getId());
            post.setStatus(PostStatusConstant.APPROVED);
         }
         postRepository.save(post);
         files.forEach(File::delete);
      });
   }

   private void saveMediaToPost(Post post, MediaResponseDto dto) {
      if (post.getMediaList() == null) {
         post.setMediaList(new ArrayList<>());
      }
      Media media = mediaRepository.findMediaByPublicId(dto.getPublicId());
      if (media == null) {
         throw new NotFoundException(ErrorMessage.Media.ERR_NOT_FOUND_MEDIA, new String[] { dto.getPublicId() });
      }
      media.setPost(post);
      post.getMediaList().add(media);
      mediaRepository.save(media);
   }
}
