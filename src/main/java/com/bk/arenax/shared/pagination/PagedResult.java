package com.bk.arenax.shared.pagination;

import com.bk.arenax.shared.constants.PaginationConstants;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record PagedResult<E>(
    List<E> items, Integer currentPage, Integer pageSize, Long totalCount) {

  public static <E> PagedResult<E> of(
      List<E> items, int currentPage, int pageSize, long totalCount) {
    return new PagedResult<>(items, currentPage, pageSize, totalCount);
  }

  public static <E> PagedResult<E> empty() {
    return new PagedResult<>(
        Collections.emptyList(),
        PaginationConstants.DEFAULT_CURRENT_PAGE,
        PaginationConstants.DEFAULT_PAGE_SIZE,
        (long) PaginationConstants.DEFAULT_TOTAL_COUNT);
  }

  public <T> PagedResult<T> map(Function<E, T> mapper) {
    List<T> mappedItems = items.stream().map(mapper).collect(Collectors.toList());
    return new PagedResult<>(mappedItems, currentPage, pageSize, totalCount);
  }
}
