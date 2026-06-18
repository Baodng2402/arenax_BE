package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
