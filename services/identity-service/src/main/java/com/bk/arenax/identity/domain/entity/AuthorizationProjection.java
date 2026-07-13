package com.bk.arenax.identity.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "authorization_projections")
public class AuthorizationProjection extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "roles_csv", nullable = false, length = 500)
    private String rolesCsv = "";

    @Column(name = "permissions_csv", nullable = false, length = 2000)
    private String permissionsCsv = "";

    @PrePersist
    void assignId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public List<String> getRoles() {
        return splitCsv(rolesCsv);
    }

    public List<String> getPermissions() {
        return splitCsv(permissionsCsv);
    }

    public void setRoles(List<String> roles) {
        this.rolesCsv = joinCsv(roles);
    }

    public void setPermissions(List<String> permissions) {
        this.permissionsCsv = joinCsv(permissions);
    }

    private static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }

    private static String joinCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .distinct()
                .collect(Collectors.joining(","));
    }
}
