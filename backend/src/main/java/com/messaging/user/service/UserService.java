package com.messaging.user.service;

import com.messaging.common.exception.ConflictException;
import com.messaging.user.dto.UserCreateRequest;
import com.messaging.user.dto.UserResponse;
import com.messaging.user.entity.User;
import com.messaging.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("User email already exists");
        }
        if (request.password() == null || !request.password().equals(request.confirmPassword())) {
            throw new ConflictException("Passwords do not match");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());

        return userRepository.save(user);
    }
}
