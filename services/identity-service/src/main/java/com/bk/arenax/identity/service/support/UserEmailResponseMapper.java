package com.bk.arenax.identity.service.support;

import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.dto.response.UserEmailResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEmailResponseMapper {

    public UserEmailResponse toResponse(UserIdentifier identifier) {
        return new UserEmailResponse(
                identifier.getId(),
                identifier.getNormalizedValue(),
                identifier.isPrimary(),
                identifier.isVerified(),
                identifier.getVerifiedAt());
    }

    public List<UserEmailResponse> toResponses(List<UserIdentifier> identifiers) {
        return identifiers.stream()
                .map(this::toResponse)
                .toList();
    }
}