package com.bk.arenax.shared.workday;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class WorkDayUtil {
  private WorkDayUtil() {}

  public static boolean isWeekend(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek();
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
  }

  public static boolean isWorkDay(LocalDate date) {
    return !isWeekend(date);
  }

  public static LocalDate nextWorkDay(LocalDate date) {
    LocalDate next = date.plusDays(1);
    while (!isWorkDay(next)) {
      next = next.plusDays(1);
    }

    return next;
  }

  public static LocalDate plusWorkDays(LocalDate date, int days) {
    LocalDate result = date;
    int added = 0;

    while (added < days) {
      result = result.plusDays(1);

      if (isWorkDay(result)) {
        added++;
      }
    }

    return result;
  }
}
