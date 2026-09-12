package com.messaging.media.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MediaHttpConfig {

  @Bean
  public RestClient storageRestClient() {
    return RestClient.builder().build();
  }
}
