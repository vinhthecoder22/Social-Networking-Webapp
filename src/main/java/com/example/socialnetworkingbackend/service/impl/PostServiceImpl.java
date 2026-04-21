package com.example.socialnetworkingbackend.service.impl;

import com.example.socialnetworkingbackend.constant.CommonConstant;
import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.PostStatusConstant;
import com.example.socialnetworkingbackend.constant.SortByDataConstant;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationFullRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationRequestDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PaginationResponseDto;
import com.example.socialnetworkingbackend.domain.dto.pagination.PagingMeta;
import com.example.socialnetworkingbackend.domain.dto.request.PostRequestDto;

import com.example.socialnetworkingbackend.domain.dto.response.PostResponseDto;
import com.example.socialnetworkingbackend.domain.entity.PostCategory;
import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.PostMapper;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.exception.NotFoundException;
import com.example.socialnetworkingbackend.repository.PostCategoryRepository;
import com.example.socialnetworkingbackend.repository.PostRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.PostCategoryService;
import com.example.socialnetworkingbackend.service.PostMediaService;
import com.example.socialnetworkingbackend.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final PostCategoryService postCategoryService;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final PostMediaService postMediaService;

    private final RedisTemplate<String, Object> redisTemplate;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    @Override
    public PostResponseDto createPost(PostRequestDto requestDto, List<File> files, List<MultipartFile> multipartFiles,
                                      List<String> contentTypeFileList)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

        if (files.isEmpty() || !files.get(0).isFile()) {
            throw new BadRequestException(ErrorMessage.Post.ERR_FILES_NULL);
        }

        if (requestDto.getMediaType() != null) {
            String contentTypeMedia = contentTypeFileList.get(0).split("/")[0];
            log.info("Content type Media: {}", contentTypeMedia);
            if (!contentTypeMedia.equals(requestDto.getMediaType().toString().toLowerCase())) {
                throw new BadRequestException(ErrorMessage.Post.ERR_FILES_INVALID_FORMAT);
            }
        }

        List<String> filePaths = files.stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        PostCategory postCategory = postCategoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category không tồn tại", new String[]{String.valueOf(requestDto.getCategoryId())}));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID,
                        new String[] { userPrincipal.getId() }));

        Post post = buildPostFromDto(requestDto, postCategory, user);
        post.setStatus(PostStatusConstant.PENDING_MODERATION);
        Post savedPost = postRepository.save(post);

        log.info("Đã tạo Post với trạng thái PENDING_MODERATION, postId: {}", savedPost.getId());

        postMediaService.processPostMedia(savedPost.getId(), filePaths, contentTypeFileList,
                requestDto.getSingerName());
        log.info("Đã gửi yêu cầu xử lý media async cho postId: {}", savedPost.getId());

        return postMapper.toPostResponseDto(savedPost);

    }

    @PreAuthorize("isAuthenticated() and @postServiceImpl.isOwner(#postId, authentication.name)")
    @Transactional
    @Override
    public void deletePost(Long postId) {
        Post post = findPostOrThrow(postId);
        postRepository.delete(post);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public PaginationResponseDto<PostResponseDto> getAllPostsByTitleKeyword(PaginationFullRequestDto request) {
        int pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        String keyword = request.getKeyword();

        Sort sort = Sort.by(request.getSortBy(SortByDataConstant.POST));
        sort = Boolean.FALSE.equals(request.getIsAscending()) ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
        Page<Post> postPage = (keyword != null && !keyword.isBlank())
                ? postRepository.searchByTitleKeyword(keyword, pageable)
                : postRepository.findAll(pageable);
        List<PostResponseDto> dtoList = postPage.stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());

        PagingMeta meta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(postPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType(request.getIsAscending() ? CommonConstant.SORT_TYPE_ASC : CommonConstant.SORT_TYPE_DESC)
                .totalElements(postPage.getTotalElements())
                .build();

        return new PaginationResponseDto<>(meta, dtoList);
    }

    @Override
    public PostResponseDto getPostById(Long postId) {
        Post post = findPostOrThrow(postId);
        PostResponseDto postResponseDto = postMapper.toPostResponseDto(post);
        if (post.getOriginalPost() != null) {
            postResponseDto.setOriginalPostId(post.getOriginalPost().getId());
        }
        return postResponseDto;
    }

    @Override
    public PaginationResponseDto<PostResponseDto> getPostsTrendingForUser(PaginationFullRequestDto request) {
        int pageSize = request.getPageSize();
        int pageNum = request.getPageNum();

        List<String> categoryNames = getTrendingCategories();
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<Post> postPage = postRepository.findByCategoryNameIn(categoryNames, pageable);

        List<PostResponseDto> dtoList = postPage.stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());

        PagingMeta meta = PagingMeta.builder()
                .pageNum(pageNum + 1)
                .pageSize(pageSize)
                .totalPages(postPage.getTotalPages())
                .sortBy(request.getSortBy())
                .sortType(request.getIsAscending() ? CommonConstant.SORT_TYPE_ASC : CommonConstant.SORT_TYPE_DESC)
                .totalElements(postPage.getTotalElements())
                .build();

        return new PaginationResponseDto(meta, dtoList);
    }

    private Post buildPostFromDto(PostRequestDto requestDto, PostCategory postCategory, User user) {
        return Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .reactionCount(0L)
                .commentCount(0L)
                .shareCount(0L)
                .category(postCategory)
                .mediaType(requestDto.getMediaType())
                .mediaList(new ArrayList<>())
                .user(user)
                .build();
    }

    private Post findPostOrThrow(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Post.ERR_NOT_FOUND_ID,
                        new String[] { String.valueOf(id) }));
    }

    public boolean isOwner(Long postId, String username) {
        Post post = findPostOrThrow(postId);
        return post.getUser().getUsername().equals(username);
    }

    private List<String> getTrendingCategories() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String redisKey = "username:" + userPrincipal.getUsername() + ":trending";

        // Lấy toàn bộ Map từ Redis Hash
        Map<Object, Object> dataTrending = redisTemplate.opsForHash().entries(redisKey);

        if (dataTrending != null && !dataTrending.isEmpty()) {
            return dataTrending.entrySet().stream()
                    .sorted((e1, e2) -> {
                        // Ép kiểu an toàn từ object của Redis
                        Long val1 = Long.parseLong(e1.getValue().toString());
                        Long val2 = Long.parseLong(e2.getValue().toString());
                        return val2.compareTo(val1); // Sắp xếp giảm dần
                    })
                    .limit(5)
                    .map(e -> e.getKey().toString())
                    .collect(Collectors.toList());
        }

        // Nếu user chưa có data, trả về Top 5 hệ thống
        List<PostCategory> postCategoryList = postCategoryRepository.findTop5ByOrderByInteractionCountDesc();
        return postCategoryList.stream()
                .map(PostCategory::getName)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @Transactional(readOnly = true)
    public PaginationResponseDto<PostResponseDto> getNewsfeed(PaginationRequestDto requestDto) {

        String currentUserId = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();

        Pageable pageable = PageRequest.of(requestDto.getPageNum(), requestDto.getPageSize());

        Page<Long> idPage = postRepository.getNewsfeedPostIds(currentUserId, pageable);

        PagingMeta metadata = PagingMeta.builder()
                .pageNum(requestDto.getPageNum())
                .pageSize(requestDto.getPageSize())
                .totalElements(idPage.getTotalElements())
                .totalPages(idPage.getTotalPages())
                .build();

        if (idPage.isEmpty()) {
            return new PaginationResponseDto<>(metadata, List.of());
        }

        List<Post> posts = postRepository.findPostsWithDetailsByIds(idPage.getContent());

        List<PostResponseDto> postList = posts.stream()
                .map(postMapper::toPostResponseDto)
                .collect(Collectors.toList());

        return new PaginationResponseDto<>(metadata, postList);
    }
}
