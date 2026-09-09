package com.messaging.app;

import com.messaging.notification.channel.EmailNotificationSender;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public abstract class IntegrationTestSupport {
  @MockitoBean protected EmailNotificationSender emailSender;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) throws Exception {
    String host = System.getenv().getOrDefault("TEST_HOST", "localhost");
    String url = "jdbc:postgresql://" + host + ":55432/messaging_test";
    registry.add("spring.datasource.url", () -> url);
    registry.add("spring.datasource.username", () -> "test");
    registry.add("spring.datasource.password", () -> "test");
    registry.add("app.datasource.write.jdbc-url", () -> url);
    registry.add("app.datasource.write.username", () -> "test");
    registry.add("app.datasource.write.password", () -> "test");
    registry.add("app.datasource.read.jdbc-url", () -> url);
    registry.add("app.datasource.read.username", () -> "test");
    registry.add("app.datasource.read.password", () -> "test");
    registry.add("spring.flyway.url", () -> url);
    registry.add("spring.flyway.user", () -> "test");
    registry.add("spring.flyway.password", () -> "test");
    registry.add("spring.data.redis.host", () -> host);
    registry.add("spring.data.redis.port", () -> 56379);
    registry.add("app.notification.async", () -> false);
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair pair = generator.generateKeyPair();
    registry.add(
        "app.security.jwt.private-key",
        () -> Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()));
    registry.add(
        "app.security.jwt.public-key",
        () -> Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));
  }
}
