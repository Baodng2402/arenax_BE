package com.bk.arenax.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Collections;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private T data;
  private String message;
  private String exceptionCode;

  protected static final String SUCCEED_REQUEST_MESSAGE = "Success";

  public static <T> ApiResponse<T> of(T data) {
    return ApiResponse.<T>builder().data(data).message(SUCCEED_REQUEST_MESSAGE).build();
  }

  public static <T> ApiResponse<T> ok() {
    return ApiResponse.<T>builder().message(SUCCEED_REQUEST_MESSAGE).build();
  }

  public static <T> ApiResponse<T> error(String exceptionCode, String exceptionMessage) {
    return ApiResponse.<T>builder().message(exceptionMessage).exceptionCode(exceptionCode).build();
  }

  @SuppressWarnings("unchecked")
  public static <T> ApiResponse<T> empty() {
    return ApiResponse.<T>builder()
        .data((T) Collections.emptyList())
        .message(SUCCEED_REQUEST_MESSAGE)
        .build();
  }
}
