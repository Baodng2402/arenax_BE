package com.bk.arenax.shared.pagination;

import com.bk.arenax.shared.constants.PaginationConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PaginationHelper {

  private PaginationHelper() {}

  public static Pageable setPage(Integer currentPage, Integer pageSize) {
    int page =
        currentPage == null || currentPage < 1
            ? PaginationConstants.DEFAULT_CURRENT_PAGE
            : currentPage;
    int size =
        pageSize == null || pageSize <= 0
            ? PaginationConstants.DEFAULT_PAGE_SIZE
            : Math.min(pageSize, PaginationConstants.MAX_PAGE_SIZE);
    return PageRequest.of(page - 1, size);
  }

  public static Pageable setPage(BasePaginationRequest request) {
    return setPage(request.getCurrentPage(), request.getPageSize());
  }
}
