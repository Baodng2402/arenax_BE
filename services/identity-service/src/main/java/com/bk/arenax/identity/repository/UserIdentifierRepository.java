package com.bk.arenax.identity.repository;

import com.bk.arenax.identity.domain.UserIdentifier;
import com.bk.arenax.identity.domain.UserIdentifierType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIdentifierRepository extends JpaRepository<UserIdentifier, UUID> {
    boolean existsByTypeAndNormalizedValue(UserIdentifierType type, String normalizedValue);

    Optional<UserIdentifier> findByTypeAndNormalizedValue(UserIdentifierType type, String normalizedValue);

    Optional<UserIdentifier> findByUserIdAndTypeAndPrimaryTrue(UUID userId, UserIdentifierType type);

    List<UserIdentifier> findAllByUserIdAndTypeOrderByPrimaryDescCreatedAtAsc(UUID userId, UserIdentifierType type);

    Optional<UserIdentifier> findByIdAndUserIdAndType(UUID id, UUID userId, UserIdentifierType type);
}
