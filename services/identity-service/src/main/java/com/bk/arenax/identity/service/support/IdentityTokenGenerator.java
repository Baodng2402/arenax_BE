package com.bk.arenax.identity.service.support;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
public class IdentityTokenGenerator {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public String generate() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
