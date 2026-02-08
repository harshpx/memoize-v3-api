package com.memoize.api.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {
   private String data;
   private LocalDateTime timestamp;

   public static ErrorResponse of(String data) {
       return ErrorResponse.builder().data(data).timestamp(LocalDateTime.now()).build();
   }
}
