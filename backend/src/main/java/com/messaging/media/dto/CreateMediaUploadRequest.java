package com.messaging.media.dto;

import com.messaging.media.entity.MediaUploadPurpose;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMediaUploadRequest(
    @NotNull MediaUploadPurpose purpose,
    @NotBlank @Size(max = 255) String fileName,
    @NotBlank @Size(max = 120) String contentType,
    @Min(1) long sizeBytes) {}
