package com.messaging.notification.channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender {

    private final JavaMailSender mailSender;
    private final EmailNotificationProperties properties;

    public void sendOtp(String destination, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFrom());
        message.setTo(destination);
        message.setSubject(properties.getOtpSubject());
        message.setText("Your verification code is " + otp + ". It expires soon.");

        mailSender.send(message);
        log.info("Email OTP sent to {}", destination);
    }
}
