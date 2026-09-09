package com.messaging.security.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
  String message() default
      "Use 8–72 characters, including uppercase, lowercase, a number and a special character (maximum 72 UTF-8 bytes)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
