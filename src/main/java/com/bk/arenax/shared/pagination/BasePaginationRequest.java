package com.bk.arenax.shared.pagination;

import com.bk.arenax.shared.constants.PaginationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BasePaginationRequest {

  @Builder.Default
  @Schema(description = "Current page (1-based)", defaultValue = "1", example = "1")
  private Integer currentPage = PaginationConstants.DEFAULT_CURRENT_PAGE;

  @Builder.Default
  @Schema(description = "Page size", defaultValue = "10", example = "10")
  private Integer pageSize = PaginationConstants.DEFAULT_PAGE_SIZE;

  public int getCurrentPage() {
    return this.currentPage != null && this.currentPage >= 1
        ? this.currentPage
        : PaginationConstants.DEFAULT_CURRENT_PAGE;
  }

  public int getPageSize() {
    if (this.pageSize == null || this.pageSize <= 0) {
      return PaginationConstants.DEFAULT_PAGE_SIZE;
    }
    return Math.min(this.pageSize, PaginationConstants.MAX_PAGE_SIZE);
  }
}
