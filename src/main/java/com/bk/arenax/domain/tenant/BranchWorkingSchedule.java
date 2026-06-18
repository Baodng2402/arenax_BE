package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "branch_working_schedules")
public class BranchWorkingSchedule extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id")
  Branch branch;

  @Enumerated(EnumType.STRING)
  DayOfWeek dayOfWeek;

  boolean workingDay;
  LocalTime openTime;
  LocalTime closeTime;

  public boolean isOpenTime(LocalTime time) {
    if (closeTime.isAfter(openTime)) return !time.isBefore(openTime) && time.isBefore(closeTime);
    return !time.isBefore(openTime) || time.isBefore(closeTime);
  }
}
