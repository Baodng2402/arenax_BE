package com.bk.arenax.identity.infrastructure.security;

import com.bk.arenax.identity.infrastructure.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)

public class SecurityConfiguration {

  @Bean
  PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecretKey jwtSecretKey(JwtProperties jwtProperties){
    return jwtProperties.secretKey();
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKey jwtSecretKey){
    return NimbusJwtEncoder
            .withSecretKey(jwtSecretKey)
            .build();
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKey jwtSecretKey,JwtProperties jwtProperties){
    NimbusJwtDecoder decoder= NimbusJwtDecoder.withSecretKey(jwtSecretKey)
            .macAlgorithm(MacAlgorithm.HS256)
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
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth->auth
                    .requestMatchers(SecurityEndpoints.PUBLIC).permitAll()
                    .anyRequest().authenticated())
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
}
