package com.bk.arenax.identity.service.support;

import com.bk.arenax.identity.domain.User;
import com.bk.arenax.identity.dto.response.UserProfileResponse;
import com.bk.arenax.identity.service.RbacService;
import com.bk.arenax.identity.service.UserEmailService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileResponseAssembler {

    private final RbacService rbacService;
    private final UserEmailService userEmailService;

    public UserProfileResponse assemble(User user, UUID accountId) {
        RbacService.RbacDetails rbac = rbacService.getUserRbac(user.getId());
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                userEmailService.requirePrimaryEmail(user.getId()).getNormalizedValue(),
                userEmailService.listEmails(user.getId()),
                user.getFullName(),
                user.getStatus().name(),
                user.getAvatarUrl(),
                user.getEmailVerifiedAt(),
                accountId,
                rbac.roles(),
                rbac.permissions());
    }
}