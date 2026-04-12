package com.example.socialnetworkingbackend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.MediaType;
import com.example.socialnetworkingbackend.domain.dto.request.PostRequestDto;
import com.example.socialnetworkingbackend.domain.dto.response.PostResponseDto;
import com.example.socialnetworkingbackend.domain.entity.Post;
import com.example.socialnetworkingbackend.domain.entity.PostCategory;
import com.example.socialnetworkingbackend.domain.entity.User;
import com.example.socialnetworkingbackend.domain.mapper.PostMapper;
import com.example.socialnetworkingbackend.exception.BadRequestException;
import com.example.socialnetworkingbackend.repository.PostCategoryRepository;
import com.example.socialnetworkingbackend.repository.PostRepository;
import com.example.socialnetworkingbackend.repository.UserRepository;
import com.example.socialnetworkingbackend.security.UserPrincipal;
import com.example.socialnetworkingbackend.service.PostMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock private PostRepository postRepository;
    @Mock private PostCategoryRepository postCategoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostMapper postMapper;
    @Mock private PostMediaService postMediaService;

    @InjectMocks
    private PostServiceImpl postService;

    private PostRequestDto requestDto;
    private User mockUser;
    private PostCategory mockCategory;
    private List<File> mockFiles;
    private List<MultipartFile> mockMultipartFiles;
    private List<String> contentTypes;

    @BeforeEach
    void setUp() throws Exception {
        requestDto = new PostRequestDto();
        requestDto.setTitle("Test Post");
        requestDto.setCategoryId(1L);
        requestDto.setMediaType(MediaType.IMAGE);

        mockCategory = new PostCategory();
        mockCategory.setId(1L);

        mockUser = new User();
        mockUser.setId("user-123");
        mockUser.setUsername("testuser");

        File tempFile = File.createTempFile("test-image", ".jpg");
        tempFile.deleteOnExit();
        mockFiles = List.of(tempFile);
        mockMultipartFiles = new ArrayList<>();
        contentTypes = List.of("image/jpeg");

        // GIẢ LẬP SECURITY CONTEXT
        UserPrincipal principal = new UserPrincipal(
                "user-123",
                "Nguyen",
                "Vinh",
                "testuser",
                "pass",
                new ArrayList<>()
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Tạo bài viết thành công với hình ảnh hợp lệ")
    void createPost_Success() throws Exception {
        // ARRANGE
        when(postCategoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(mockUser));

        Post savedPost = new Post();
        savedPost.setId(99L);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostResponseDto responseDto = new PostResponseDto();
        responseDto.setId(99L);
        when(postMapper.toPostResponseDto(any(Post.class))).thenReturn(responseDto);

        // ACT
        PostResponseDto result = postService.createPost(requestDto, mockFiles, mockMultipartFiles, contentTypes);

        // ASSERT
        assertNotNull(result);
        assertEquals(99L, result.getId());
        // Kiểm tra xem Service có gọi lệnh xử lý File nền không
        verify(postMediaService, times(1)).processPostMedia(eq(99L), anyList(), anyList(), any());
    }

    @Test
    @DisplayName("Tạo bài viết thất bại: Sai định dạng file (Bắt lỗi Media Type)")
    void createPost_Fail_InvalidFormat() {
        // ARRANGE: Người dùng truyền File PDF, nhưng MediaType ở Request yêu cầu là IMAGE
        contentTypes = List.of("application/pdf");

        // ACT & ASSERT
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            postService.createPost(requestDto, mockFiles, mockMultipartFiles, contentTypes);
        });

        assertEquals(ErrorMessage.Post.ERR_FILES_INVALID_FORMAT, exception.getMessage());
        verify(postRepository, never()).save(any(Post.class)); // Đảm bảo rác không lọt vào DB
    }
}