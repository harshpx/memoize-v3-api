package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.ChatDto;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.ConversationDto;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

public interface ChatController {
    ResponseEntity<CommonResponse<List<ConversationDto>>> getConversationsOfUser(AuthPrincipal principal);
    ResponseEntity<CommonResponse<ConversationDto>> createNewConversationForUser(AuthPrincipal principal);
    ResponseEntity<CommonResponse<ConversationDto>> getConversationByIdAndUser(UUID conversationId, AuthPrincipal principal);
    ResponseEntity<CommonResponse<List<ChatDto>>> getChatsOfConversation(UUID conversationId, AuthPrincipal principal);
    ResponseEntity<Flux<String>> askLLM(String query, UUID conversationId, AuthPrincipal principal);
}
