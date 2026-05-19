package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.AiChatDto;
import com.memoize.api.Dto.CommonResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiController {
    ResponseEntity<CommonResponse<List<AiChatDto>>> getLlmChatsOfUser(AuthPrincipal principal);
    ResponseEntity<CommonResponse<String>> llmChat(String query, AuthPrincipal principal);
    ResponseEntity<Flux<String>> llmChatStream(String query, AuthPrincipal principal);
}
