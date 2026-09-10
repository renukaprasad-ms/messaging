package com.messaging.media.storage;

import com.messaging.common.exception.ApiException;
import com.messaging.media.config.MediaStorageProperties;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class SupabaseS3StorageSigner implements StorageSigner {

  private final MediaStorageProperties properties;

  public SupabaseS3StorageSigner(MediaStorageProperties properties) {
    this.properties = properties;
  }

  @Override
  public SignedUpload signUpload(String path, String contentType) {
    if (!StringUtils.hasText(properties.getEndpoint())
        || !StringUtils.hasText(properties.getAccessKey())
        || !StringUtils.hasText(properties.getSecretKey())) {
      throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Media storage is not configured");
    }

    try (S3Presigner presigner = createPresigner()) {
      PutObjectRequest objectRequest =
          PutObjectRequest.builder()
              .bucket(properties.getBucket())
              .key(path)
              .contentType(contentType)
              .cacheControl("max-age=3600")
              .build();
      PutObjectPresignRequest presignRequest =
          PutObjectPresignRequest.builder()
              .signatureDuration(properties.getSignedUploadTtl())
              .putObjectRequest(objectRequest)
              .build();
      return new SignedUpload("", presigner.presignPutObject(presignRequest).url().toString());
    }
  }

  private S3Presigner createPresigner() {
    return S3Presigner.builder()
        .endpointOverride(URI.create(properties.getEndpoint()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())))
        .region(Region.of(properties.getRegion()))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .build();
  }
}
