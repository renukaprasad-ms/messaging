package com.messaging.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.messaging.common.exception.ServiceUnavailableException;
import com.messaging.common.exception.TooManyRequestsException;
import com.messaging.notification.model.NotificationChannel;
import com.messaging.notification.service.OtpService;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;

class OtpIntegrationTests extends IntegrationTestSupport {
  @Autowired OtpService otpService;

  @Test
  void aCodeCanBeConsumedOnlyOnceEvenConcurrently() throws Exception {
    String email = UUID.randomUUID() + "@example.com";
    otpService.sendOtp(email, NotificationChannel.EMAIL, OtpService.Purpose.EMAIL_VERIFICATION);
    var capture = ArgumentCaptor.forClass(String.class);
    verify(emailSender).sendOtp(eq(email), capture.capture());
    var start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      Callable<Boolean> verify =
          () -> {
            start.await();
            try {
              return otpService.verifyOtp(
                  email,
                  NotificationChannel.EMAIL,
                  OtpService.Purpose.EMAIL_VERIFICATION,
                  capture.getValue());
            } catch (RuntimeException expected) {
              return false;
            }
          };
      var first = executor.submit(verify);
      var second = executor.submit(verify);
      start.countDown();
      assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
    }
    assertThrows(
        TooManyRequestsException.class,
        () ->
            otpService.sendOtp(
                email, NotificationChannel.EMAIL, OtpService.Purpose.EMAIL_VERIFICATION));
  }

  @Test
  void codesCannotCrossPurposesAndAttemptsAreLimited() {
    String email = UUID.randomUUID() + "@example.com";
    otpService.sendOtp(email, NotificationChannel.EMAIL, OtpService.Purpose.EMAIL_VERIFICATION);
    var capture = ArgumentCaptor.forClass(String.class);
    verify(emailSender).sendOtp(eq(email), capture.capture());
    assertThrows(
        RuntimeException.class,
        () ->
            otpService.verifyOtp(
                email,
                NotificationChannel.EMAIL,
                OtpService.Purpose.PASSWORD_RESET,
                capture.getValue()));
    String wrong = capture.getValue().equals("000000") ? "111111" : "000000";
    for (int i = 0; i < 5; i++)
      assertFalse(
          otpService.verifyOtp(
              email, NotificationChannel.EMAIL, OtpService.Purpose.EMAIL_VERIFICATION, wrong));
    assertThrows(
        TooManyRequestsException.class,
        () ->
            otpService.verifyOtp(
                email,
                NotificationChannel.EMAIL,
                OtpService.Purpose.EMAIL_VERIFICATION,
                capture.getValue()));
  }

  @Test
  void deliveryFailureLetsTheUserRetry() {
    String email = UUID.randomUUID() + "@example.com";
    doThrow(new ServiceUnavailableException("Delivery failed"))
        .doNothing()
        .when(emailSender)
        .sendOtp(eq(email), anyString());
    assertThrows(
        ServiceUnavailableException.class,
        () ->
            otpService.sendOtp(
                email, NotificationChannel.EMAIL, OtpService.Purpose.EMAIL_VERIFICATION));
    assertDoesNotThrow(
        () ->
            otpService.sendOtp(
                email, NotificationChannel.EMAIL, OtpService.Purpose.EMAIL_VERIFICATION));
  }
}
