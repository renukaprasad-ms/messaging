package com.messaging.notification.channel;

import com.messaging.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsNotificationSender {

  public void sendOtp(String destination, String otp) {
    throw new ServiceUnavailableException("SMS delivery is not configured. Please use email.");
  }
}
