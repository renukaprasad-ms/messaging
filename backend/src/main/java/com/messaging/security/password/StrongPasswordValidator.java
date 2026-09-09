package com.messaging.security.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

/** A bounded, single-pass check; BCrypt must never silently truncate a password. */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null
        || password.length() < 8
        || password.length() > 72
        || password.getBytes(StandardCharsets.UTF_8).length > 72) return false;
    boolean upper = false, lower = false, number = false, special = false;
    for (int i = 0; i < password.length(); i++) {
      char character = password.charAt(i);
      upper |= character >= 'A' && character <= 'Z';
      lower |= character >= 'a' && character <= 'z';
      number |= character >= '0' && character <= '9';
      special |=
          (character >= '!' && character <= '/')
              || (character >= ':' && character <= '@')
              || (character >= '[' && character <= '`')
              || (character >= '{' && character <= '~');
    }
    return upper && lower && number && special;
  }
}
