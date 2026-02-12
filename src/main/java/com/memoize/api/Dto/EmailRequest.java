package com.memoize.api.Dto;

import lombok.*;

import java.util.List;

@Builder
public record EmailRequest(List<String> recipients, String subject, String body) {
}
