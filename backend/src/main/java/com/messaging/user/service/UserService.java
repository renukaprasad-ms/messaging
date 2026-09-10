package com.messaging.user.service;

import com.messaging.auth.dto.LoginResponse;
import com.messaging.common.exception.BadRequestException;
import com.messaging.common.exception.ConflictException;
import com.messaging.common.exception.NotFoundException;
import com.messaging.company.service.CompanyMembershipService;
import com.messaging.user.dto.UserCreateRequest;
import com.messaging.user.dto.UserPage;
import com.messaging.user.dto.UserUpdateRequest;
import com.messaging.user.entity.User;
import com.messaging.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EntityManager entityManager;
  private final CompanyMembershipService companyMembershipService;

  public Optional<User> findByIdentifier(String identifier) {
    return userRepository.findByEmailOrPhone(identifier, identifier);
  }

  @Transactional
  public Optional<User> findAuthenticatedUser(Long userId, String accessKey, Instant now) {
    return userRepository.findAuthenticatedUser(userId, accessKey, now);
  }

  @Transactional(readOnly = true)
  public UserPage listUsers(int page) {
    if (page < 0) throw new BadRequestException("Page must not be negative");
    var users = userRepository.listSummaries(PageRequest.of(page, 25));
    return new UserPage(users.getContent(), users.hasNext());
  }

  public User getForUpdate(Long id) {
    User user =
        userRepository
            .findForUpdateById(id)
            .orElseThrow(() -> new NotFoundException("User not found"));
    entityManager.refresh(user);
    return user;
  }

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
    user.setProfilePhotoUrl(request.profilePhotoUrl());
    user.setAccountType(request.accountType());

    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public User getById(Long id) {
    return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
  }

  @Transactional
  public User updateProfile(Long id, UserUpdateRequest request) {
    User user = getForUpdate(id);
    user.setName(request.name().trim());
    user.setPhone(request.phone());
    user.setProfilePhotoUrl(request.profilePhotoUrl());
    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public LoginResponse toLoginResponse(User user) {
    boolean hasCompany = companyMembershipService.hasActiveMembership(user);
    return new LoginResponse(
        user.getName(),
        user.getEmail(),
        user.getPhone(),
        user.getProfilePhotoUrl(),
        user.getAccountType(),
        hasCompany,
        user.getStatus(),
        user.getPlatformRoles().stream()
            .filter(role -> role.isActive())
            .map(role -> role.getName())
            .collect(Collectors.toSet()),
        user.isPasswordChangeRequired());
  }

  public void save(User user) {
    userRepository.save(user);
  }
}
