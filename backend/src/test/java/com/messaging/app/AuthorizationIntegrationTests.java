package com.messaging.app;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.messaging.auth.dto.LoginRequest;
import com.messaging.auth.service.AuthService;
import com.messaging.company.dto.CompanyCreateRequest;
import com.messaging.company.entity.CompanyMembership;
import com.messaging.company.repository.CompanyMembershipRepository;
import com.messaging.company.repository.CompanyRepository;
import com.messaging.company.service.CompanyService;
import com.messaging.platformrole.repository.PlatformRoleRepository;
import com.messaging.role.repository.RoleRepository;
import com.messaging.session.dto.SessionRequestMetadata;
import com.messaging.session.entity.SessionPlatform;
import com.messaging.user.entity.User;
import com.messaging.user.entity.UserStatus;
import com.messaging.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AuthorizationIntegrationTests extends IntegrationTestSupport {
  @Autowired MockMvc mvc;
  @Autowired UserRepository users;
  @Autowired CompanyRepository companies;
  @Autowired RoleRepository roles;
  @Autowired PlatformRoleRepository platformRoles;
  @Autowired CompanyMembershipRepository memberships;
  @Autowired CompanyService companyService;
  @Autowired AuthService auth;
  @Autowired PasswordEncoder encoder;
  private static final String PASSWORD = "Test-password-1!";
  private static final SessionRequestMetadata META =
      new SessionRequestMetadata(SessionPlatform.WEB, "test", "test", "127.0.0.1", "test");

  private User account(boolean verified) {
    var user = new User();
    user.setName("Test");
    user.setEmail(UUID.randomUUID() + "@example.com");
    user.setPassword(encoder.encode(PASSWORD));
    user.setEmailVerified(verified);
    user.setStatus(verified ? UserStatus.ACTIVE : UserStatus.PENDING_VERIFICATION);
    return users.save(user);
  }

  private Cookie cookie(User user) {
    return new Cookie(
        "msid", auth.login(new LoginRequest(user.getEmail(), PASSWORD, false), META).accessToken());
  }

  @Test
  void guestsGet401AndMutationsRequireCsrf() throws Exception {
    mvc.perform(get("/api/companies")).andExpect(status().isUnauthorized());
    mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void passwordValidationIsEnforcedAtTheHttpBoundary() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {"name":"Test","email":"valid@example.com","password":"weak","confirmPassword":"weak"}
                        """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void verificationAndPlatformRolesGateEntry() throws Exception {
    var pending = account(false);
    mvc.perform(get("/api/companies").cookie(cookie(pending))).andExpect(status().isForbidden());
    var regular = account(true);
    var regularCookie = cookie(regular);
    mvc.perform(get("/api/admin/users").cookie(regularCookie)).andExpect(status().isForbidden());
    regular.getPlatformRoles().add(platformRoles.findByName("ADMIN").orElseThrow());
    regular = users.save(regular);
    mvc.perform(get("/api/admin/users").cookie(regularCookie)).andExpect(status().isOk());
    regular.setPasswordChangeRequired(true);
    regular = users.save(regular);
    mvc.perform(get("/api/admin/users").cookie(regularCookie)).andExpect(status().isForbidden());
    regular.setStatus(UserStatus.DISABLED);
    users.save(regular);
    mvc.perform(get("/api/auth/me").cookie(regularCookie)).andExpect(status().isUnauthorized());
  }

  @Test
  void companyAccessIsScopedToTheRequestedCompanyAndRole() throws Exception {
    var owner = account(true);
    var company =
        companyService.createCompany(
            new CompanyCreateRequest(
                "Test",
                "Test",
                "Test Ltd",
                null,
                null,
                null,
                null,
                null,
                null,
                "Street",
                null,
                "City",
                null,
                null,
                "India"),
            owner.getId());
    var outsider = account(true);
    mvc.perform(get("/api/companies/" + company.id()).cookie(cookie(outsider)))
        .andExpect(status().isForbidden());
    var membership = new CompanyMembership();
    membership.setUser(outsider);
    membership.setCompany(companies.findById(company.id()).orElseThrow());
    membership.setRole(roles.findByName("MANAGER").orElseThrow());
    memberships.save(membership);
    var managerCookie = cookie(outsider);
    mvc.perform(get("/api/companies/" + company.id()).cookie(managerCookie))
        .andExpect(status().isOk());
    mvc.perform(
            patch("/api/companies/" + company.id())
                .cookie(managerCookie)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Changed\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            patch("/api/companies/" + company.id())
                .cookie(cookie(owner))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Changed\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.name").value("Changed"));
  }
}
