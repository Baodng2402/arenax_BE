package com.bk.arenax.subscription.infrastructure.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.bk.arenax.security.trustedgateway.TrustedGatewayAuthenticationFilter;
import com.bk.arenax.security.trustedgateway.TrustedGatewayFilterMode;

@Configuration
public class SecurityConfiguration implements WebMvcConfigurer {

  private final CurrentUserResolver currentUserResolver;

  public SecurityConfiguration(CurrentUserResolver currentUserResolver) {
    this.currentUserResolver = currentUserResolver;
  }

  @Bean
  TrustedGatewayAuthenticationFilter trustedGatewayAuthenticationFilter() {
    return new TrustedGatewayAuthenticationFilter(TrustedGatewayFilterMode.REQUIRED);
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, TrustedGatewayAuthenticationFilter trustedGatewayAuthenticationFilter)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers("/actuator/health")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(trustedGatewayAuthenticationFilter, AnonymousAuthenticationFilter.class);
    return http.build();
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(currentUserResolver);
  }
}
