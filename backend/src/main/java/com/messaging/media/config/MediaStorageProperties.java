package com.messaging.media.config;

import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.media.storage")
public class MediaStorageProperties {

  private String provider = "supabase";
  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String region = "us-east-1";
  private String publicBaseUrl;
  private String bucket = "messaging-assets";
  private Duration signedUploadTtl = Duration.ofHours(2);
  private long maxUploadBytes = 5 * 1024 * 1024;
  private List<String> allowedContentTypes =
      List.of(
          "image/jpeg",
          "image/png",
          "image/webp",
          "image/gif",
          "application/pdf",
          "text/plain",
          "text/csv",
          "video/mp4",
          "audio/mpeg");
}
