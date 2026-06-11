package com.bk.arenax.domain.tenant;

import com.bk.arenax.domain.common.BaseEntity;
import com.bk.arenax.domain.match.Sport;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "courts")
public class Court extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id", referencedColumnName = "id")
  Branch branch;

  String name;
  @Enumerated(EnumType.STRING)
  CourtSetting setting;
  @Enumerated(EnumType.STRING)
  CourtStatus status;
  BigDecimal basePrice;
  String description;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sport_id", referencedColumnName = "id")
  Sport sport;

  public boolean isBookable(){
    return status== CourtStatus.ACTIVE;
  }
}
