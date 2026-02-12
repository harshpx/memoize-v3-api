package com.memoize.api.Dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(String data, LocalDateTime timestamp) {
   public static ErrorResponse of(String data) {
       return ErrorResponse.builder().data(data).timestamp(LocalDateTime.now()).build();
   }
}
