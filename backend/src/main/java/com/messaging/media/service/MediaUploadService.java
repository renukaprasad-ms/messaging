package com.messaging.media.service;

import com.messaging.common.exception.BadRequestException;
import com.messaging.media.config.MediaStorageProperties;
import com.messaging.media.dto.CreateMediaUploadRequest;
import com.messaging.media.dto.MediaUploadResponse;
import com.messaging.media.storage.StorageSigner;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MediaUploadService {

  private static final Map<String, String> EXTENSIONS_BY_CONTENT_TYPE =
      Map.of(
          "image/jpeg", "jpg",
          "image/png", "png",
          "image/webp", "webp",
          "image/gif", "gif",
          "application/pdf", "pdf",
          "text/plain", "txt",
          "text/csv", "csv",
          "video/mp4", "mp4",
          "audio/mpeg", "mp3");

  private final MediaStorageProperties properties;
  private final StorageSigner storageSigner;

  public MediaUploadService(MediaStorageProperties properties, StorageSigner storageSigner) {
    this.properties = properties;
    this.storageSigner = storageSigner;
  }

  public MediaUploadResponse createUpload(CreateMediaUploadRequest request, Long userId) {
    validate(request);
    String path =
        request.purpose().folder()
            + "/"
            + userId
            + "/"
            + UUID.randomUUID()
            + "."
            + extension(request.fileName(), request.contentType());
    var signedUpload = storageSigner.signUpload(path, request.contentType());
    return new MediaUploadResponse(
        properties.getBucket(),
        path,
        signedUpload.token(),
        signedUpload.signedUrl(),
        publicUrl(path),
        properties.getSignedUploadTtl().toSeconds());
  }

  private void validate(CreateMediaUploadRequest request) {
    if (request.sizeBytes() > properties.getMaxUploadBytes()) {
      throw new BadRequestException("File is larger than the allowed upload size");
    }
    if (!properties.getAllowedContentTypes().contains(request.contentType())) {
      throw new BadRequestException("File type is not allowed");
    }
    if (request.purpose().imageOnly() && !request.contentType().startsWith("image/")) {
      throw new BadRequestException("This upload purpose only accepts images");
    }
  }

  private String extension(String fileName, String contentType) {
    String extension = "";
    int dot = fileName.lastIndexOf('.');
    if (dot >= 0 && dot < fileName.length() - 1) {
      extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
    String expectedExtension = EXTENSIONS_BY_CONTENT_TYPE.get(contentType);
    if (expectedExtension == null) {
      throw new BadRequestException("File type is not allowed");
    }
    if (!StringUtils.hasText(extension)) extension = expectedExtension;
    if (extension.equals("jpeg")) extension = "jpg";
    if (!extension.equals(expectedExtension)) {
      throw new BadRequestException("File extension is not allowed");
    }
    return extension;
  }

  private String publicUrl(String path) {
    String baseUrl =
        StringUtils.hasText(properties.getPublicBaseUrl())
            ? properties.getPublicBaseUrl()
            : properties
                .getEndpoint()
                .replace(
                    ".storage.supabase.co/storage/v1/s3", ".supabase.co/storage/v1/object/public");
    return baseUrl.replaceAll("/+$", "") + "/" + properties.getBucket() + "/" + path;
  }
}
