package com.memoize.api.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponse<T> {
    private T data;
    private boolean success;
    private LocalDateTime timestamp;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder().data(data).success(true)
                .timestamp(LocalDateTime.now()).build();
    }

    // unused but might be useful later
    public static CommonResponse<String> error(String message) {
        return CommonResponse.<String>builder().data(message).success(false)
                .timestamp(LocalDateTime.now()).build();
    }
}
