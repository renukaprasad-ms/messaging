package com.messaging.media.service;

import com.messaging.media.config.MediaValidationProperties;
import com.messaging.media.entity.Media;
import com.messaging.media.enums.MediaStatus;
import com.messaging.media.provider.StorageProviderResolver;
import com.messaging.media.repository.MediaRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaCleanupService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaCleanupService.class);

  private final MediaRepository mediaRepository;
  private final MediaValidationProperties validationProperties;
  private final StorageProviderResolver storageProviderResolver;

  public int cleanupExpiredPendingUploads() {
    Instant cutoff = Instant.now().minus(validationProperties.getPendingTtl());
    int cleaned = 0;
    for (Media media :
        mediaRepository.findByStatusAndCreatedAtBefore(MediaStatus.PENDING, cutoff)) {
      try {
        if (storageProviderResolver
            .resolve(media.getStorageProvider())
            .getObjectMetadata(media.getBucket(), media.getObjectKey())
            .exists()) {
          storageProviderResolver
              .resolve(media.getStorageProvider())
              .delete(media.getBucket(), media.getObjectKey());
        }
      } catch (RuntimeException exception) {
        LOGGER.warn(
            "Expired pending media cleanup storage step failed mediaId={}",
            media.getId(),
            exception);
      }
      media.setStatus(MediaStatus.FAILED);
      mediaRepository.saveAndFlush(media);
      cleaned++;
    }
    return cleaned;
  }
}
