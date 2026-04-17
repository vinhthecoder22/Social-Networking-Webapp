package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationFullRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.request.PostRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.PostResponseDto;
import com.example.socialnetworkingbackend.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestApiV1
@RequiredArgsConstructor
@Validated
@Tag(name = "API bài viết")
public class PostController {

    private final PostService postService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "Tạo bài viết mới", description = "- `data`: thông tin bài viết dưới dạng JSON String (application/json)\n"
            +
            "- `files`: danh sách file (chỉ 1 video, 1 audio hoặc nhiều ảnh)\n" +
            "- `audio`: Nếu upload audio thì audio phải ở đầu danh sách file, rồi đến 1 file image")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo bài viết thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping(value = UrlConstant.Post.CREATE_NEW_POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNewPost(
            @Parameter(description = "Thông tin bài viết (JSON String)", required = true, schema = @Schema(type = "string", format = "json", example = "{\"title\": \"Tiêu đề\", \"content\": \"Nội dung\", \"category\": \"Tên danh mục\", \"mediaType\": \"IMAGE\", \"singerName\": \"Tên ca sĩ (nếu có)\"}")) @RequestPart("data") String dataStr,

            @Parameter(description = "Danh sách file upload", required = true) @RequestPart("files") List<MultipartFile> files)
            throws IOException, ExecutionException, InterruptedException, TimeoutException {

        log.info("Received dataStr: {}", dataStr);
        PostRequestDto requestDto;
        try {
            requestDto = objectMapper.readValue(dataStr, PostRequestDto.class);
        } catch (JsonProcessingException e) {
            log.error("JSON Error: {}", e.getMessage());
            throw new com.example.socialnetworkingbackend.exception.BadRequestException(
                    "Invalid JSON format in 'data' part: " + e.getMessage());
        }

        // Thực hiện kiểm tra Validation thủ công sau khi Parse JSON
        Set<ConstraintViolation<PostRequestDto>> violations = validator.validate(requestDto);
        if (!violations.isEmpty()) {
            // Lấy lỗi đầu tiên và ném ra
            String errorMessage = violations.iterator().next().getMessage();
            throw new com.example.socialnetworkingbackend.exception.BadRequestException(errorMessage);
        }

        log.info("transfer To original File");
        List<File> copiedFiles = new ArrayList<>();
        List<String> contentTypeList = new ArrayList<>();
        for (MultipartFile multipartFile : files) {
            contentTypeList.add(multipartFile.getContentType());
            File tempFile = File.createTempFile("_upload_", multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);
            copiedFiles.add(tempFile);
        }
        log.info("successfully transfer To original File");
        PostResponseDto responseDto = postService.createPost(requestDto, copiedFiles, files, contentTypeList);
        return VsResponseUtil.success(HttpStatus.CREATED, responseDto);
    }

    @Operation(summary = "Lấy tất cả bài viết theo từ khóa tiêu đề (có phân trang)")
    @GetMapping(UrlConstant.Post.GET_ALL_POST_BY_TITLE)
    public ResponseEntity<?> getAllPostsByTitleKeyword(
            @Valid @ModelAttribute PaginationFullRequestDto request) {
        PaginationResponseDto<PostResponseDto> result = postService.getAllPostsByTitleKeyword(request);
        return VsResponseUtil.success(HttpStatus.OK, result);
    }

    @Operation(summary = "Lấy thông tin bài viết theo ID")
    @GetMapping(path = UrlConstant.Post.GET_POST)
    public ResponseEntity<?> getPostById(@PathVariable("id") Long id) {
        PostResponseDto post = postService.getPostById(id);
        return VsResponseUtil.success(post);
    }

    @GetMapping(path = UrlConstant.Post.GET_TRENDING_POST)
    public ResponseEntity<?> getTrendingPost(@ModelAttribute @Valid PaginationFullRequestDto request)
            throws JsonProcessingException {
        PaginationResponseDto<PostResponseDto> response = postService.getPostsTrendingForUser(request);
        return VsResponseUtil.success(HttpStatus.OK, response);
    }

    @Operation(summary = "Xóa bài viết theo ID")
    @DeleteMapping(path = UrlConstant.Post.DELETE_POST)
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return VsResponseUtil.success(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Lấy bảng tin (Newsfeed) của người dùng hiện tại")
    @GetMapping(UrlConstant.Post.GET_NEWSFEED)
    public ResponseEntity<?> getNewsfeed(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        PaginationRequestDto requestDto = PaginationRequestDto.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();

        return VsResponseUtil.success(postService.getNewsfeed(requestDto));
    }
}