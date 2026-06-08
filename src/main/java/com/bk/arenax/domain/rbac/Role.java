package com.bk.arenax.domain.rbac;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(
    name = "roles",
    uniqueConstraints = @UniqueConstraint(name = "uk_roles_code_name", columnNames = "code_name"))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role extends BaseEntity {

  @Column(nullable = false, length = 120)
  String name;

  @Column(name = "code_name", nullable = false, length = 80)
  String codeName;

  @Column(length = 500)
  String description;

  @Column(name = "system_role", nullable = false)
  boolean systemRole;

  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "role_permissions",
      joinColumns = @JoinColumn(name = "role_id"),
      inverseJoinColumns = @JoinColumn(name = "permission_id"))
  @Builder.Default
  Set<Permission> permissions = new HashSet<>();

  public static Role of(String name, String codeName) {
    Role role = new Role();
    role.name = StringUtils.trimToEmpty(name);
    role.codeName = normalizeCodeName(codeName);
    return role;
  }

  public String authorityName() {
    return "ROLE_" + codeName;
  }

  public void grantPermission(Permission permission) {
    validatePermission(permission);
    permissions.add(permission);
  }

  public void revokePermission(Permission permission) {
    validatePermission(permission);
    permissions.remove(permission);
  }

  public void replacePermissions(Set<Permission> permissions) {
    this.permissions.clear();
    this.permissions.addAll(permissions == null ? Set.of() : permissions);
  }

  public void rename(String name, String description) {
    this.name = StringUtils.trimToEmpty(name);
    this.description = description;
  }

  private void validatePermission(Permission permission) {
    if (permission == null || !StringUtils.isNotBlank(permission.getCodeName())) {
      throw new IllegalArgumentException("Wrong permission format");
    }
  }

  public static String normalizeCodeName(String value) {
    if (!StringUtils.isNotBlank(value)) {
      throw new IllegalArgumentException("Role code must not be blank");
    }
    String normalized = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
    return normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
  }
}
