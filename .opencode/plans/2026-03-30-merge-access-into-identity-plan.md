# Merge Access Service Into Identity Service Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Merge `access-service` RBAC domain entities (`Role`, `Permission`, `RoleAssignment`), repositories, and event handlers into `identity-service`, embed `roles` and `permissions` into JWT claims, and clean up the separate `access-service`.

**Architecture:** Combine AuthN (Identity) and AuthZ (Access/RBAC) into a single consolidated `identity-service` bounded context. Update JWT generation (`JwtService`) to include `roles` and `permissions` claims. Migrate Flyway migrations and domain models from `access-service` to `identity-service`.

**Tech Stack:** Java 17+, Spring Boot, Spring Security (OAuth2 Resource Server / JWT), Spring Data JPA, Flyway, Gradle.

## Global Constraints
- Keep exact naming conventions and package structures consistent with existing `identity-service`.
- Ensure Flyway migrations are merged cleanly without ID collisions.
- Preserve all existing tests and add new characterization tests for JWT claims embedding.

---

### Task 1: Migrate RBAC Domain Entities and Flyway Migration to Identity Service

**Files:**
- Create: `services/identity-service/src/main/resources/db/migration/V6__add_rbac_core.sql`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/domain/Role.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/domain/Permission.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/domain/RoleAssignment.java`
- Test: `services/identity-service/src/test/java/com/bk/arenax/identity/RbacDomainIntegrationTests.java`

**Interfaces:**
- Consumes: Existing base entity and database configuration in `identity-service`.
- Produces: `Role`, `Permission`, and `RoleAssignment` entities in `com.bk.arenax.identity.domain`.

- [ ] **Step 1: Write Flyway migration V6__add_rbac_core.sql**

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE role_assignments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id UUID NOT NULL,
    role_code VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **Step 2: Create Role, Permission, and RoleAssignment entity classes**

```java
package com.bk.arenax.identity.domain;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    @Id
    private UUID id;
    @Column(nullable = false, length = 80, unique = true)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new LinkedHashSet<>();
    @PrePersist void assignId() { if (id == null) id = UUID.randomUUID(); }
}
```

- [ ] **Step 3: Commit migration and domain entities**

```bash
git add services/identity-service/src/main/resources/db/migration/V6__add_rbac_core.sql services/identity-service/src/main/java/com/bk/arenax/identity/domain/
git commit -m "feat(identity): add RBAC domain entities and migration"
```

---

### Task 2: Implement RBAC Repositories and Services in Identity Service

**Files:**
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/repository/RoleRepository.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/repository/PermissionRepository.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/repository/RoleAssignmentRepository.java`
- Create: `services/identity-service/src/main/java/com/bk/arenax/identity/service/RbacService.java`
- Test: `services/identity-service/src/test/java/com/bk/arenax/identity/RbacServiceTests.java`

**Interfaces:**
- Consumes: `Role`, `Permission`, `RoleAssignment` from Task 1.
- Produces: `RbacService` method `getUserRolesAndPermissions(UUID userId)`.

- [ ] **Step 1: Create RoleRepository, PermissionRepository, RoleAssignmentRepository**

```java
package com.bk.arenax.identity.repository;

import com.bk.arenax.identity.domain.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(String code);
}
```

- [ ] **Step 2: Create RbacService to query user roles and permissions**

```java
package com.bk.arenax.identity.service;

import com.bk.arenax.identity.domain.Permission;
import com.bk.arenax.identity.domain.Role;
import com.bk.arenax.identity.repository.RoleAssignmentRepository;
import com.bk.arenax.identity.repository.RoleRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class RbacService {
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final RoleRepository roleRepository;

    public RbacService(RoleAssignmentRepository roleAssignmentRepository, RoleRepository roleRepository) {
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public RbacDetails getUserRbac(UUID userId) {
        List<String> roleCodes = roleAssignmentRepository.findByUserId(userId).stream()
                .map(ra -> ra.getRoleCode())
                .distinct()
                .collect(Collectors.toList());

        List<String> permissions = roleCodes.stream()
                .flatMap(code -> roleRepository.findByCode(code).stream())
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .collect(Collectors.toList());

        return new RbacDetails(roleCodes, permissions);
    }

    public record RbacDetails(List<String> roles, List<String> permissions) {}
}
```

- [ ] **Step 3: Commit repositories and RbacService**

```bash
git add services/identity-service/src/main/java/com/bk/arenax/identity/repository/ services/identity-service/src/main/java/com/bk/arenax/identity/service/RbacService.java
git commit -m "feat(identity): add RBAC repositories and RbacService"
```

---

### Task 3: Embed Roles & Permissions into JWT Claims

**Files:**
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/infrastructure/jwt/JwtService.java`
- Modify: `services/identity-service/src/main/java/com/bk/arenax/identity/service/UserService.java` (or authentication handler)
- Test: `services/identity-service/src/test/java/com/bk/arenax/identity/JwtServiceTests.java`

**Interfaces:**
- Consumes: `RbacService` from Task 2.
- Produces: JWT access token containing `roles` and `permissions` claims.

- [ ] **Step 1: Update JwtService to accept roles and permissions claims**

```java
public String generateAccessToken(User user, List<String> roles, List<String> permissions) {
    Instant now = Instant.now();
    Instant validity = now.plusMillis(jwtProperties.getAccessTokenExpirationMs());
    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("arenax-identity")
            .issuedAt(now)
            .expiresAt(validity)
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .claim("roles", roles)
            .claim("permissions", permissions)
            .build();
    return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
}
```

- [ ] **Step 2: Integrate RbacService into UserService token generation flow**

Call `rbacService.getUserRbac(user.getId())` during login/refresh token generation and pass `roles` and `permissions` to `jwtService.generateAccessToken(...)`.

- [ ] **Step 3: Commit JWT RBAC integration**

```bash
git add services/identity-service/src/main/java/com/bk/arenax/identity/infrastructure/jwt/JwtService.java services/identity-service/src/main/java/com/bk/arenax/identity/service/UserService.java
git commit -m "feat(identity): embed roles and permissions into JWT claims"
```

---

### Task 4: Remove Separate Access Service and Clean Up Monorepo

**Files:**
- Remove: `services/access-service/` directory
- Modify: Root `build.gradle.kts` / `settings.gradle.kts` to remove `access-service` subproject.
- Test: Run `./gradlew test` across the workspace.

- [ ] **Step 1: Remove access-service directory and update gradle settings**

Remove `services/access-service` and update `settings.gradle.kts` to exclude `access-service`.

- [ ] **Step 2: Run verification tests**

Run: `./gradlew test`
Expected: ALL tests pass successfully.

- [ ] **Step 3: Commit cleanup**

```bash
git add settings.gradle.kts
git rm -r services/access-service
git commit -m "refactor: remove access-service and consolidate RBAC into identity-service"
```
