package com.bk.arenax.identity.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.jwk.JWKSet;

@RestController
public class JwksController {

  private final JWKSet publicJwkSet;

  public JwksController(JWKSet publicJwkSet) {
    this.publicJwkSet = publicJwkSet;
  }

  @GetMapping("/.well-known/jwks.json")
  public Map<String, Object> jwks() {
    return publicJwkSet.toJSONObject();
  }
}
