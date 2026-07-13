package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.entity.User;
import com.bk.arenax.identity.domain.enums.UserStatus;
import com.bk.arenax.identity.dto.request.LoginRequest;
import com.bk.arenax.identity.dto.response.TokenResponse;
import com.bk.arenax.identity.exception.InvalidCredentialsException;
import com.bk.arenax.identity.exception.UserOnboardingIncompleteException;
import com.bk.arenax.identity.repository.AuthorizationProjectionRepository;
import com.bk.arenax.identity.repository.UserRepository;
import com.bk.arenax.identity.security.JwtService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final AuthorizationProjectionRepository authorizationProjectionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(
            UserRepository userRepository,
            AuthorizationProjectionRepository authorizationProjectionRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.authorizationProjectionRepository = authorizationProjectionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() == UserStatus.PROVISIONING) {
            throw new UserOnboardingIncompleteException();
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new InvalidCredentialsException();
        }

        com.bk.arenax.identity.domain.entity.AuthorizationProjection projection = authorizationProjectionRepository
                .findFirstByUserIdOrderByCreatedAtAsc(user.getId())
                .orElseThrow(InvalidCredentialsException::new);

        String accessToken = jwtService.issueAccessToken(
                user.getId(),
                user.getActiveAccountId(),
                projection.getRoles(),
                projection.getPermissions());
        return new TokenResponse(accessToken, "", jwtService.getAccessTokenTtlSeconds());
    }
}
