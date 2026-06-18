package com.bk.arenax.application.tenant.pipeline;

import com.bk.arenax.domain.tenant.BranchWorkingSchedule;
import java.time.DayOfWeek;
import java.time.LocalTime;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class GenerateDefaultScheduleStep implements TenantCreationStep {

  private static final LocalTime DEFAULT_OPEN = LocalTime.of(6, 0);
  private static final LocalTime DEFAULT_CLOSE = LocalTime.of(22, 0);

  @Override
  public void execute(TenantCreationContext context) {
    var branch = context.getFirstBranch();
    for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
      var schedule = new BranchWorkingSchedule();
      schedule.setDayOfWeek(dayOfWeek);
      schedule.setBranch(branch);
      schedule.setWorkingDay(true);
      schedule.setOpenTime(DEFAULT_OPEN);
      schedule.setCloseTime(DEFAULT_CLOSE);
      branch.getWorkingSchedules().add(schedule);
    }
  }
}
