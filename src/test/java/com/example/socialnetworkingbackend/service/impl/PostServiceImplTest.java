package com.example.socialnetworkingbackend.service.impl;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostCategoryRepository postCategoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostMapper postMapper;
    @Mock
    private PostMediaService postMediaService;

    @InjectMocks
    private PostServiceImpl postService;

    private PostRequestDto requestDto;
    private User mockUser;
    private PostCategory mockCategory;
    private List<File> mockFiles;
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
        contentTypes = List.of("image/jpeg");

        UserPrincipal principal = new UserPrincipal(
                "user-123",
                "Nguyen",
                "Vinh",
                "testuser",
                "pass",
                new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create post succeeds with valid image input")
    void createPost_Success() throws Exception {
        when(postCategoryRepository.findById(1L)).thenReturn(Optional.of(mockCategory));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(mockUser));

        Post savedPost = new Post();
        savedPost.setId(99L);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostResponseDto responseDto = new PostResponseDto();
        responseDto.setId(99L);
        when(postMapper.toPostResponseDto(any(Post.class))).thenReturn(responseDto);

        PostResponseDto result = postService.createPost(requestDto, mockFiles, contentTypes);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        verify(postMediaService, times(1)).processPostMedia(eq(99L), anyList(), anyList(), any());
    }

    @Test
    @DisplayName("Create post fails when media type does not match file content type")
    void createPost_Fail_InvalidFormat() {
        contentTypes = List.of("application/pdf");

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> postService.createPost(requestDto, mockFiles, contentTypes));

        assertEquals(ErrorMessage.Post.ERR_FILES_INVALID_FORMAT, exception.getMessage());
        verify(postRepository, never()).save(any(Post.class));
    }
}
