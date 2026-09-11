package com.messaging.user.service;

import com.messaging.common.exception.ConflictException;
import com.messaging.user.dto.CreateUserRequest;
import com.messaging.user.entity.User;
import com.messaging.user.repository.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public User createUser(CreateUserRequest request) {
    String email = normalizeEmail(request.email());
    String username = normalizeUsername(request.username());

    userRepository
        .findByEmailOrUsername(email, username)
        .ifPresent(
            existingUser -> {
              if (existingUser.getEmail().equals(email)) {
                throw new ConflictException("Email already exists");
              }
              throw new ConflictException("Username already exists");
            });

    User user = new User();
    user.setEmail(email);
    user.setName(request.name().trim());
    user.setVerified(false);
    user.setProfilePicture(normalizeOptional(request.profilePicture()));
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setTwoFactorEnabled(request.twoFactorEnabled());

    try {
      return userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException exception) {
      throw new ConflictException("Email or username already exists");
    }
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String normalizeUsername(String username) {
    return username.trim().toLowerCase(Locale.ROOT);
  }

  private String normalizeOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
