package com.memoize.api.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    private List<String> recipients;
    private String subject;
    private String body;
}
