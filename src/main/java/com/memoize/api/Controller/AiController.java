package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.AiChatDto;
import com.memoize.api.Dto.CommonResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

public interface AiController {
    ResponseEntity<CommonResponse<Page<AiChatDto>>> getLlmChatsOfUser(AuthPrincipal principal, Pageable pageable);
    ResponseEntity<CommonResponse<String>> llmChat(String query, AuthPrincipal principal);
    ResponseEntity<Flux<String>> llmChatStream(String query, AuthPrincipal principal);
}
