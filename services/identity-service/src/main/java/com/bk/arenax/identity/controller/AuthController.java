package com.bk.arenax.identity.controller;

import com.bk.arenax.identity.dto.request.LoginRequest;
import com.bk.arenax.identity.dto.request.RegisterRequest;
import com.bk.arenax.identity.dto.response.RegisteredUserResponse;
import com.bk.arenax.identity.dto.response.TokenResponse;
import com.bk.arenax.identity.service.AuthenticationService;
import com.bk.arenax.identity.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return registrationService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }
}
