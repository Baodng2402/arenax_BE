package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "branch_day_overrides")
public class BranchDayOverride extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id")
  Branch branch;

  LocalDate date;

  boolean workingDay;

  LocalTime openTime;

  LocalTime closeTime;

  String reason;
}
