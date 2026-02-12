package com.memoize.api.Dto;

import jakarta.validation.constraints.Null;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CommonResponse<T>(T data, boolean success, LocalDateTime timestamp) {
    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder().data(data).success(true)
                .timestamp(LocalDateTime.now()).build();
    }

    public static CommonResponse<Void> successWithoutData() {
        return CommonResponse.<Void>builder().data(null).success(true).timestamp(LocalDateTime.now()).build();
    }

    public static CommonResponse<String> error(String message) {
        return CommonResponse.<String>builder().data(message).success(false)
                .timestamp(LocalDateTime.now()).build();
    }
}
