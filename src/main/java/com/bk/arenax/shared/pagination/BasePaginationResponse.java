package com.bk.arenax.shared.pagination;

import com.bk.arenax.dto.response.ApiResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BasePaginationResponse<T> extends ApiResponse<List<T>> {

  private PagedResponse pagination;

  private BasePaginationResponse(List<T> data, PagedResponse pagination) {
    this.pagination = pagination;
    super.setData(data);
    super.setMessage(SUCCEED_REQUEST_MESSAGE);
  }

  public static <T> BasePaginationResponse<T> of(List<T> content, long total, int page, int size) {
    int totalPages = size <= 0 ? 1 : (int) Math.ceil((double) total / size);
    return new BasePaginationResponse<>(
        content,
        PagedResponse.builder()
            .page(page)
            .size(size)
            .numberOfElements(content.size())
            .totalElements(total)
            .totalPages(totalPages)
            .first(page <= 1)
            .last(page >= totalPages)
            .empty(content.isEmpty())
            .build());
  }

  public static <T> BasePaginationResponse<T> of(
      List<T> content, long total, BasePaginationRequest request) {
    return of(content, total, request.getCurrentPage(), request.getPageSize());
  }

  public static <T> BasePaginationResponse<T> of(List<T> content, PagedResult<?> source) {
    return of(content, source.totalCount(), source.currentPage(), source.pageSize());
  }

  public static <T> BasePaginationResponse<T> of(PagedResult<T> result) {
    return of(result.items(), result.totalCount(), result.currentPage(), result.pageSize());
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class PagedResponse {
    private int page;
    private int size;
    private int numberOfElements;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;
  }
}
