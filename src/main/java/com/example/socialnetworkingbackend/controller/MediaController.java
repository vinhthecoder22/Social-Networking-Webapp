package com.example.socialnetworkingbackend.controller;

import com.example.socialnetworkingbackend.base.RestApiV1;
import com.example.socialnetworkingbackend.base.VsResponseUtil;
import com.example.socialnetworkingbackend.constant.ErrorMessage;
import com.example.socialnetworkingbackend.constant.UrlConstant;
import com.example.socialnetworkingbackend.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestApiV1
@Validated
@RequiredArgsConstructor
@Tag(name = "API media", description = "Các API liên quan về upload, get, delete media")
public class MediaController {

    private final MediaService mediaService;

    @Operation(summary = "API Delete Media")
    @DeleteMapping(UrlConstant.Media.DELETE_MEDIA)
    public ResponseEntity<?> deleteMedia(@RequestParam("mediaId") List<String> mediaId) {
        if (mediaService.deleteMedia(mediaId)) {
            return VsResponseUtil.success(HttpStatus.NO_CONTENT);
        }
        return VsResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.ERR_EXCEPTION_GENERAL);
    }
}
