package com.messaging.media.service;

import com.messaging.common.id.IdGenerator;
import com.messaging.media.config.MediaStorageProperties;
import com.messaging.media.dto.InitiateMediaUploadRequest;
import com.messaging.media.dto.InitiateMediaUploadResponse;
import com.messaging.media.dto.MediaAccessResponse;
import com.messaging.media.dto.MediaResponse;
import com.messaging.media.entity.Media;
import com.messaging.media.enums.MediaStatus;
import com.messaging.media.exception.MediaException;
import com.messaging.media.mapper.MediaMapper;
import com.messaging.media.provider.SignedDownloadResult;
import com.messaging.media.provider.SignedUploadRequest;
import com.messaging.media.provider.SignedUploadResult;
import com.messaging.media.provider.StorageObjectMetadata;
import com.messaging.media.provider.StorageProvider;
import com.messaging.media.provider.StorageProviderResolver;
import com.messaging.media.repository.MediaRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MediaService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaService.class);

  private final MediaRepository mediaRepository;
  private final CurrentUserService currentUserService;
  private final MediaValidationService validationService;
  private final MediaObjectKeyFactory objectKeyFactory;
  private final StorageProviderResolver storageProviderResolver;
  private final MediaStorageProperties storageProperties;
  private final MediaAuthorizationService authorizationService;
  private final MediaMapper mediaMapper;

  public InitiateMediaUploadResponse initiateUpload(InitiateMediaUploadRequest request) {
    long userId = currentUserService.currentUserId();
    MediaValidationService.ValidatedMedia validated =
        validationService.validateForUpload(
            request.fileName(), request.contentType(), request.sizeBytes());

    Media media = createPendingMedia(userId, request, validated);
    StorageProvider provider = storageProviderResolver.resolve(media.getStorageProvider());
    SignedUploadResult signedUpload =
        provider.createSignedUpload(
            new SignedUploadRequest(
                media.getBucket(),
                media.getObjectKey(),
                media.getContentType(),
                media.getSizeBytes(),
                storageProperties.getUploadUrlExpiry()));

    LOGGER.info(
        "Media upload initiated mediaId={} userId={} provider={} purpose={} status={}",
        media.getId(),
        userId,
        media.getStorageProvider(),
        media.getPurpose(),
        media.getStatus());

    return new InitiateMediaUploadResponse(
        media.getId().toString(),
        signedUpload.uploadUrl(),
        signedUpload.expiresAt(),
        signedUpload.requiredHeaders());
  }

  @Transactional
  protected Media createPendingMedia(
      long userId,
      InitiateMediaUploadRequest request,
      MediaValidationService.ValidatedMedia validated) {
    long mediaId = IdGenerator.nextId();
    Media media = new Media();
    media.setId(mediaId);
    media.setOwnerUserId(userId);
    media.setStorageProvider(storageProperties.getDefaultProvider());
    media.setBucket(storageProperties.getSupabase().getBucket());
    media.setObjectKey(
        objectKeyFactory.createUserObjectKey(
            userId, request.purpose(), mediaId, validated.extension()));
    media.setOriginalFileName(validated.fileName());
    media.setContentType(validated.contentType());
    media.setSizeBytes(validated.sizeBytes());
    media.setMediaType(validated.mediaType());
    media.setPurpose(request.purpose());
    media.setStatus(MediaStatus.PENDING);
    return mediaRepository.saveAndFlush(media);
  }

  public MediaResponse completeUpload(String mediaId) {
    long userId = currentUserService.currentUserId();
    Media media = loadOwnedMedia(parseMediaId(mediaId), userId);
    if (media.getStatus() == MediaStatus.ACTIVE) {
      return mediaMapper.toResponse(media);
    }
    if (media.getStatus() != MediaStatus.PENDING) {
      throw new MediaException(HttpStatus.CONFLICT, "MEDIA_INVALID_STATE");
    }

    StorageProvider provider = storageProviderResolver.resolve(media.getStorageProvider());
    StorageObjectMetadata metadata =
        provider.getObjectMetadata(media.getBucket(), media.getObjectKey());
    try {
      if (!metadata.exists()) {
        markFailed(media.getId());
        throw new MediaException(HttpStatus.BAD_REQUEST, "MEDIA_STORAGE_OBJECT_MISSING");
      }
      validationService.validateUploadedObject(
          media.getContentType(),
          media.getSizeBytes(),
          metadata.contentType(),
          metadata.sizeBytes());
    } catch (MediaException exception) {
      markFailed(media.getId());
      throw exception;
    }

    return mediaMapper.toResponse(markActive(media.getId(), metadata.checksum()));
  }

  public MediaAccessResponse createAccessUrl(String mediaId) {
    long userId = currentUserService.currentUserId();
    Media media = loadOwnedMedia(parseMediaId(mediaId), userId);
    if (media.getStatus() != MediaStatus.ACTIVE) {
      throw new MediaException(HttpStatus.CONFLICT, "MEDIA_NOT_ACTIVE");
    }
    StorageProvider provider = storageProviderResolver.resolve(media.getStorageProvider());
    SignedDownloadResult signedDownload =
        provider.createSignedDownloadUrl(
            media.getBucket(), media.getObjectKey(), storageProperties.getDownloadUrlExpiry());
    LOGGER.info(
        "Media access URL generated mediaId={} userId={} provider={} purpose={}",
        media.getId(),
        userId,
        media.getStorageProvider(),
        media.getPurpose());
    return new MediaAccessResponse(signedDownload.url(), signedDownload.expiresAt());
  }

  public MediaResponse delete(String mediaId) {
    long userId = currentUserService.currentUserId();
    Media media = loadOwnedMedia(parseMediaId(mediaId), userId);
    if (media.getStatus() == MediaStatus.DELETED) {
      return mediaMapper.toResponse(media);
    }
    Media deleted = markDeleted(media.getId());
    try {
      storageProviderResolver
          .resolve(deleted.getStorageProvider())
          .delete(deleted.getBucket(), deleted.getObjectKey());
    } catch (MediaException exception) {
      LOGGER.warn(
          "Physical media delete failed mediaId={} userId={} provider={}",
          deleted.getId(),
          userId,
          deleted.getStorageProvider(),
          exception);
    }
    return mediaMapper.toResponse(deleted);
  }

  @Transactional(readOnly = true)
  public Media loadOwnedMedia(long mediaId, long userId) {
    Media media =
        mediaRepository
            .findById(mediaId)
            .orElseThrow(() -> new MediaException(HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND"));
    authorizationService.requireOwner(media, userId);
    return media;
  }

  @Transactional(readOnly = true)
  public Media getActiveOwnedMedia(long mediaId, long userId) {
    Media media = loadOwnedMedia(mediaId, userId);
    if (media.getStatus() != MediaStatus.ACTIVE) {
      throw new MediaException(HttpStatus.CONFLICT, "MEDIA_NOT_ACTIVE");
    }
    return media;
  }

  @Transactional
  protected Media markActive(long mediaId, String checksum) {
    Media media =
        mediaRepository
            .findById(mediaId)
            .orElseThrow(() -> new MediaException(HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND"));
    if (media.getStatus() == MediaStatus.ACTIVE) {
      return media;
    }
    if (media.getStatus() != MediaStatus.PENDING) {
      throw new MediaException(HttpStatus.CONFLICT, "MEDIA_INVALID_STATE");
    }
    media.setStatus(MediaStatus.ACTIVE);
    media.setCompletedAt(Instant.now());
    media.setChecksum(checksum);
    return mediaRepository.saveAndFlush(media);
  }

  @Transactional
  protected void markFailed(long mediaId) {
    mediaRepository
        .findById(mediaId)
        .filter(media -> media.getStatus() == MediaStatus.PENDING)
        .ifPresent(
            media -> {
              media.setStatus(MediaStatus.FAILED);
              mediaRepository.saveAndFlush(media);
            });
  }

  @Transactional
  protected Media markDeleted(long mediaId) {
    Media media =
        mediaRepository
            .findById(mediaId)
            .orElseThrow(() -> new MediaException(HttpStatus.NOT_FOUND, "MEDIA_NOT_FOUND"));
    media.setStatus(MediaStatus.DELETED);
    media.setDeletedAt(Instant.now());
    return mediaRepository.saveAndFlush(media);
  }

  private long parseMediaId(String mediaId) {
    try {
      return Long.parseLong(mediaId);
    } catch (NumberFormatException exception) {
      throw new MediaException(HttpStatus.BAD_REQUEST, "MEDIA_INVALID_ID");
    }
  }
}
