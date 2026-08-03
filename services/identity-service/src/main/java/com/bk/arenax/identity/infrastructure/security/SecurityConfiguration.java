package com.bk.arenax.identity.infrastructure.security;

import com.bk.arenax.identity.infrastructure.jwt.JwtProperties;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CookieProperties.class})

public class SecurityConfiguration {

  @Bean
  PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  RSAPublicKey jwtPublicKey(JwtProperties jwtProperties){
    return readPublicKey(jwtProperties.publicKeyLocation());
  }

  @Bean
  RSAPrivateKey jwtPrivateKey(JwtProperties jwtProperties){
    return readPrivateKey(jwtProperties.privateKeyLocation());
  }

  @Bean
  RSAKey rsaKey(JwtProperties jwtProperties, RSAPublicKey publicKey, RSAPrivateKey privateKey){
    return new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(jwtProperties.keyId())
            .build();
  }

  @Bean
  JWKSet publicJwkSet(RSAKey rsaKey){
    return new JWKSet(rsaKey.toPublicJWK());
  }

  @Bean
  JwtEncoder jwtEncoder(RSAKey rsaKey){
    return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
  }

  @Bean
  JwtDecoder jwtDecoder(RSAPublicKey jwtPublicKey,JwtProperties jwtProperties){
    NimbusJwtDecoder decoder= NimbusJwtDecoder.withPublicKey(jwtPublicKey)
            .signatureAlgorithm(SignatureAlgorithm.RS256)
            .build();
    OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators
            .createDefaultWithIssuer(jwtProperties.issuer());
    OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
            jwt.getAudience()
                    .contains(jwtProperties.audience()) ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(
                            new OAuth2Error(
                                    "invalid_token",
                                    "Required audience is missing",
                                    null
                            )
            );
    decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
            issuerValidator,
            audienceValidator
    ));
    return decoder;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
                                          TrustedGatewayAuthenticationFilter trustedGatewayAuthenticationFilter) throws Exception{
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth->auth
                    .requestMatchers(SecurityEndpoints.PUBLIC).permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(trustedGatewayAuthenticationFilter,
                    org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .oauth2ResourceServer(oauth2->oauth2.jwt(
                    Customizer.withDefaults()
            ))
            .cors(Customizer.withDefaults())
            .sessionManagement(session->session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return  http.build();
  }

  @Bean
  AuthenticationManager authenticationManager(IdentityUserDetailsService userDetailsService,
                                              PasswordEncoder passwordEncoder){
    var provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);

    return new ProviderManager(provider);
  }

  private static RSAPrivateKey readPrivateKey(Resource resource) {
    try {
      byte[] keyBytes = decodePem(resource, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
      return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("Failed to parse RSA private key", exception);
    }
  }

  private static RSAPublicKey readPublicKey(Resource resource) {
    try {
      byte[] keyBytes = decodePem(resource, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
      return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
    } catch (GeneralSecurityException exception) {
      throw new IllegalStateException("Failed to parse RSA public key", exception);
    }
  }

  private static byte[] decodePem(Resource resource, String beginMarker, String endMarker) {
    try {
      String pem = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      String normalized = pem.replace(beginMarker, "")
              .replace(endMarker, "")
              .replaceAll("\\s", "");
      return Base64.getDecoder().decode(normalized);
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to read key resource " + resource, exception);
    }
  }
}
