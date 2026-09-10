package com.messaging.media.controller;

import com.messaging.common.response.ApiResponse;
import com.messaging.media.dto.CreateMediaUploadRequest;
import com.messaging.media.dto.MediaUploadResponse;
import com.messaging.media.service.MediaUploadService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/media/uploads")
public class MediaUploadController {

  private final MediaUploadService mediaUploadService;

  public MediaUploadController(MediaUploadService mediaUploadService) {
    this.mediaUploadService = mediaUploadService;
  }

  @PostMapping("/signed-url")
  public ApiResponse<MediaUploadResponse> createSignedUpload(
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody CreateMediaUploadRequest request) {
    return ApiResponse.success(
        201,
        mediaUploadService.createUpload(request, Long.valueOf(userId)),
        "Signed upload URL created");
  }
}
