package com.bk.arenax.tenant.infrastructure.security;

import com.bk.arenax.security.trustedgateway.TrustedGatewayAuthenticationFilter;
import com.bk.arenax.security.trustedgateway.TrustedGatewayFilterMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

    @Bean
    TrustedGatewayAuthenticationFilter trustedGatewayAuthenticationFilter() {
        return new TrustedGatewayAuthenticationFilter(TrustedGatewayFilterMode.OPTIONAL);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, TrustedGatewayAuthenticationFilter trustedGatewayAuthenticationFilter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(trustedGatewayAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
