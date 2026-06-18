package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.account.Account;
import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;
import java.util.Arrays;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {
  String companyName;
  String taxCode;

  @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  List<Branch> branches;

  String description;
  String logoUrl;
  String thumbnailUrl;
  String email;
  String address;
  String phone;
  Double latitude;
  Double longitude;
  String socialLink;

  @Enumerated(EnumType.STRING)
  @Setter(AccessLevel.NONE)
  TenantStatus status;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", referencedColumnName = "id")
  Account account;

  public void approve() {
    requireStatus(TenantStatus.PENDING);
    this.status = TenantStatus.APPROVED;
  }

  public void reject() {
    requireStatus(TenantStatus.PENDING);
    this.status = TenantStatus.REJECTED;
  }

  public void resubmit() {
    requireStatus(TenantStatus.REJECTED);
    this.status = TenantStatus.PENDING;
  }

  public void publish() {
    requireStatus(TenantStatus.APPROVED, TenantStatus.SUSPENDED);
    this.status = TenantStatus.PUBLISHED;
  }

  public void suspend() {
    requireStatus(TenantStatus.PUBLISHED);
    this.status = TenantStatus.SUSPENDED;
  }

  public void submit() {
    this.status = TenantStatus.PENDING;
  }

  private void requireStatus(TenantStatus... allowed) {
    if (Arrays.stream(allowed).noneMatch(s -> s == this.status))
      throw new IllegalArgumentException("Wrong pipeline status");
  }

  public void addBranch(Branch branch) {
    branches.add(branch);
    branch.setTenant(this);
  }
  public void test(){
    StringBuilder s = new StringBuilder();
    s.length();
  }
}
