package com.messaging.notification.channel;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

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
    }
}
