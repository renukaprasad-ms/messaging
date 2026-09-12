package com.messaging.media.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MediaCleanupScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaCleanupScheduler.class);
  private final MediaCleanupService mediaCleanupService;

  @Scheduled(
      initialDelayString = "${app.media.cleanup.initial-delay:1m}",
      fixedDelayString = "${app.media.cleanup.interval:12h}")
  public void cleanupExpiredPendingUploads() {
    int totalDeleted = mediaCleanupService.cleanupExpiredPendingUploads();
    if (totalDeleted > 0) {
      LOGGER.info("Expired pending media cleanup completed deleted={}", totalDeleted);
    }
  }
}
