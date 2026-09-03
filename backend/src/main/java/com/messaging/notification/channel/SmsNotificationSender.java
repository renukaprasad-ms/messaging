package com.messaging.notification.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsNotificationSender {

    public void sendOtp(String destination, String otp) {
        log.info("Sending SMS OTP to {}", destination);
    }
}
