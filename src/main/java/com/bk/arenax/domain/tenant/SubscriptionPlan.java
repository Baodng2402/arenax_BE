package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "subscription_plans")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubscriptionPlan extends BaseEntity {
  String code;
  String name;
  BigDecimal pricePerMonth;
  int durationDays;
  int maxBranches;
  int maxCourts;
}
