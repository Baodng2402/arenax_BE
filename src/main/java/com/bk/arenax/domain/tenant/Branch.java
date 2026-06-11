package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "branches")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Branch extends BaseEntity {
  @ManyToOne( fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", referencedColumnName = "id")
  Tenant tenant;

  String name;
  Double latitude;
  Double longitude;
  String avatarUrl;
  String thumbnailUrl;
  String phone;
  String address;
  String socialLink;

  @OneToMany(mappedBy="branch",cascade = CascadeType.ALL,orphanRemoval=true)
  List<Court> courts = new ArrayList<>();
  String timezone = "Asia/Ho_Chi_Minh";

  @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BranchWorkingSchedule> workingSchedules = new ArrayList<>();

  @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BranchDayOverride> dayOverrides = new ArrayList<>();

  public Boolean isOpening(LocalDate day, LocalTime time) {
    return dayOverrides.stream()
        .filter(override -> override.getDate().equals(day))
        .findFirst()
        .map(
            override ->
                override.isWorkingDay()
                    && !time.isBefore(override.getOpenTime())
                    && time.isBefore(override.getCloseTime()))
        .orElseGet(
            () ->
                workingSchedules.stream()
                    .filter(ws -> ws.getDayOfWeek().equals(day.getDayOfWeek()))
                    .anyMatch(ws -> ws.isWorkingDay() && ws.isOpenTime(time)));
  }
  public void addCourt(Court court){
    courts.add(court);
    court.setBranch(this);
  }
}
