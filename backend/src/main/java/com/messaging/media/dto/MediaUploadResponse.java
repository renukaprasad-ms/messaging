package com.messaging.media.dto;

public record MediaUploadResponse(
    String bucket,
    String path,
    String token,
    String signedUrl,
    String publicUrl,
    long expiresInSeconds) {}
