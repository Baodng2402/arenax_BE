package com.bk.arenax.identity.infrastructure.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@ConfigurationProperties(prefix = "arenax.security.jwt")
public record JwtProperties (
        String issuer,
        String audience,
        long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds,
        String secret){
  public SecretKey secretKey(){
    byte[] keyBytes = Base64.getDecoder().decode(secret);
    return new SecretKeySpec(keyBytes,"HmacSHA256");
  }



}