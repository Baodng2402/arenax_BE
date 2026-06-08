package com.bk.arenax.domain.rbac;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "permissions",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_permissions_code_name", columnNames = "code_name"))
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends BaseEntity {

  @Column(nullable = false, length = 120)
  String name;

  @Column(name = "code_name", nullable = false, length = 120)
  String codeName;

  @Column(nullable = false, length = 80)
  String module;

  @Column(nullable = false, length = 80)
  String action;

  @Column(length = 500)
  String description;

  public static Permission of(String name, String codeName, String module, String action) {
    Permission permission = new Permission();
    permission.name = StringUtils.trimToEmpty(name);
    permission.codeName = normalizeCode(codeName);
    permission.module = normalizeCode(module);
    permission.action = normalizeCode(action);
    return permission;
  }

  public void rename(String name, String description) {
    this.name = StringUtils.trimToEmpty(name);
    this.description = description;
  }

  public static String normalizeCode(String value) {
    if (!StringUtils.isNotBlank(value)) {
      throw new IllegalArgumentException("Permission code must not be blank");
    }
    return value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
  }
}
