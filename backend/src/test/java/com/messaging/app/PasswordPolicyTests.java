package com.messaging.app;

import static org.junit.jupiter.api.Assertions.*;

import com.messaging.security.password.StrongPasswordValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordPolicyTests {
  private final StrongPasswordValidator validator = new StrongPasswordValidator();

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(
      strings = {"Aa1!abc", "abcdefgh!1", "ABCDEFGH!1", "Abcdefgh!", "Abcdefgh1", "Abcdef1 "})
  void rejectsPasswordsMissingRequirements(String password) {
    assertFalse(validator.isValid(password, null));
  }

  @Test
  void enforcesBothLengthBoundariesAndByteLimit() {
    assertTrue(validator.isValid("Abcdef1!", null));
    assertTrue(validator.isValid("Aa1!" + "x".repeat(68), null));
    assertFalse(validator.isValid("Aa1!" + "x".repeat(69), null));
    assertFalse(validator.isValid("Aa1!" + "é".repeat(35), null));
  }
}
