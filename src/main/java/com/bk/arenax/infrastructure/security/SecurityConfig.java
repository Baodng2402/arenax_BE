package com.bk.arenax.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static com.bk.arenax.infrastructure.security.SecurityConstants.PUBLIC_APIS;

@Configuration
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Value("${application.security.cors.allowed-origins:http://localhost:3000}")
  private String allowedOrigins;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_APIS).permitAll()
             .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                    (request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value())
            ))
            .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
    config.setAllowedHeaders(Arrays.asList(SecurityConstants.CORS_ALLOWED_HEADERS));
    config.setExposedHeaders(Arrays.asList(SecurityConstants.CORS_EXPOSED_HEADERS));
    config.setAllowedMethods(Arrays.asList(SecurityConstants.CORS_ALLOWED_METHODS));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource url = new UrlBasedCorsConfigurationSource();
    url.registerCorsConfiguration(SecurityConstants.CORS_PATH_PATTERNS, config);
    return url;
  }
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

}
