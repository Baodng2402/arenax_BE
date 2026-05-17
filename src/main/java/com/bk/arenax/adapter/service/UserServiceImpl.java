package com.bk.arenax.adapter.service;

import com.bk.arenax.adapter.repository.JpaAccountRepository;
import com.bk.arenax.adapter.repository.JpaSubscriptionRepository;
import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.account.AccountStatus;
import com.bk.arenax.domain.account.AccountType;
import com.bk.arenax.domain.subscription.Subscription;
import com.bk.arenax.domain.subscription.SubscriptionPlan;
import com.bk.arenax.domain.subscription.SubscriptionStatus;
import com.bk.arenax.dto.request.CreateUserRequest;
import com.bk.arenax.dto.request.UpdateUserRequest;
import com.bk.arenax.domain.user.User;
import com.bk.arenax.dto.response.UserResponse;
import com.bk.arenax.infrastructure.exception.DuplicateResourceException;
import com.bk.arenax.infrastructure.exception.ExceptionConstants;
import com.bk.arenax.infrastructure.exception.ResourceNotFoundException;
import com.bk.arenax.port.repository.UserRepository;
import com.bk.arenax.port.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JpaAccountRepository accountRepository;
    private final JpaSubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(this::toUserResponse)
                .orElseThrow(() -> userNotFound(id));
    }

    private UserResponse toUserResponse(User user) {
        UserResponse.AccountInfo accountInfo = null;
        Account account = user.getAccount();
        if (account != null) {
            Subscription subscription = account.getSubscription();
            UserResponse.SubscriptionInfo subscriptionInfo = subscription == null ? null : new UserResponse.SubscriptionInfo(
                    subscription.getId(),
                    subscription.getPlan(),
                    subscription.getStatus(),
                    subscription.getStartedAt(),
                    subscription.getExpiresAt(),
                    subscription.getCancelledAt()
            );
            accountInfo = new UserResponse.AccountInfo(
                    account.getId(),
                    account.getName(),
                    account.getType(),
                    account.getStatus(),
                    subscriptionInfo
            );
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getFullName(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                user.getAvatarUrl(),
                user.getEmail(),
                user.getGender(),
                user.getStatus(),
                user.getRole(),
                accountInfo
        );
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())){
            throw new DuplicateResourceException(
                    ExceptionConstants.EMAIL_ALREADY_EXISTS,
                    "Email already exists: " + request.email());
        } else {
            User user = new User();
            user.setName(request.name());
            user.setFullName(firstNonBlank(request.fullName(), request.name()));
            user.setDisplayName(firstNonBlank(request.displayName(), request.name()));
            user.setPhoneNumber(request.phoneNumber());
            user.setAvatarUrl(request.avatarUrl());
            user.setEmail(request.email());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setGender(request.gender());
            User savedUser = userRepository.save(user);

            Account account = new Account(
                    firstNonBlank(savedUser.getDisplayName(), savedUser.getName()) + "'s Account",
                    AccountType.PERSONAL,
                    AccountStatus.ACTIVE,
                    savedUser
            );
            Account savedAccount = accountRepository.save(account);

            Subscription subscription = new Subscription(savedAccount, SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE);
            Subscription savedSubscription = subscriptionRepository.save(subscription);
            savedAccount.setSubscription(savedSubscription);

            savedUser.setAccount(savedAccount);
            return toUserResponse(userRepository.save(savedUser));
        }

    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User existingUser = userRepository.findById(id).orElseThrow(() -> userNotFound(id));
            existingUser.setName(request.name());
            existingUser.setFullName(firstNonBlank(request.fullName(), request.name()));
            existingUser.setDisplayName(firstNonBlank(request.displayName(), request.name()));
            existingUser.setPhoneNumber(request.phoneNumber());
            existingUser.setAvatarUrl(request.avatarUrl());
            existingUser.setEmail(request.email());
            existingUser.setGender(request.gender());
            return toUserResponse(userRepository.save(existingUser));
    }

    @Override
    public void deleteUser(Long id) {
        if(userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw userNotFound(id);
        }
    }

    private ResourceNotFoundException userNotFound(Long id) {
        return new ResourceNotFoundException(
                ExceptionConstants.USER_NOT_FOUND,
                "User not found with id: " + id);
    }

    private String firstNonBlank(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
