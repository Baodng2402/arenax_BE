package com.bk.arenax.dto.response;

import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApiResponse<T> {
    private static final String SUCCESS_MESSAGE = "Success";

    private T data;
    private String message;
    private String exceptionCode;

    public static <T> ApiResponse<T> of(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .message(SUCCESS_MESSAGE)
                .build();
    }

    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder()
                .message(SUCCESS_MESSAGE)
                .build();
    }

    public static <T> ApiResponse<T> error(String exceptionCode, String exceptionMessage) {
        return ApiResponse.<T>builder()
                .message(exceptionMessage)
                .exceptionCode(exceptionCode)
                .build();
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> empty() {
        return ApiResponse.<T>builder()
                .data((T) Collections.emptyList())
                .message(SUCCESS_MESSAGE)
                .build();
    }
}
