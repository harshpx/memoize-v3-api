package com.memoize.api.Controller;

import com.memoize.api.Config.Security.AuthPrincipal;
import com.memoize.api.Dto.ChatDto;
import com.memoize.api.Dto.CommonResponse;
import com.memoize.api.Dto.ConversationDto;
import com.memoize.api.Service.ChatService;
import com.memoize.api.Service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatControllerImpl implements ChatController {
    private final ConversationService conversationService;
    private final ChatService chatService;

    @Override
    @GetMapping("/conversation/{id}")
    public ResponseEntity<CommonResponse<ConversationDto>> getConversationByIdAndUser(@PathVariable(name = "id") UUID conversationId, @AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(CommonResponse.success(conversationService.getConversationByIdForUser(conversationId, principal.userId())));
    }
    @Override
    @PostMapping("/conversation/all")
    public ResponseEntity<CommonResponse<List<ConversationDto>>> getConversationsOfUser(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(CommonResponse.success(conversationService.getConversationsOfUser(principal.userId())));
    }

    @Override
    @PostMapping("/conversation")
    public ResponseEntity<CommonResponse<ConversationDto>> createNewConversationForUser(@AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(CommonResponse.success(conversationService.createNewConversationForUser(principal.userId())));
    }

    @Override
    @DeleteMapping("/conversation/{id}")
    public ResponseEntity<CommonResponse<Integer>> deleteConversationByIdAndUser(@PathVariable(name = "id") UUID conversationId, @AuthenticationPrincipal AuthPrincipal principal) {
        return ResponseEntity.ok(CommonResponse.success(conversationService.deleteConversationByIdAndUser(conversationId, principal.userId())));
    }

    @Override
    @GetMapping("/chat/{id}")
    public ResponseEntity<CommonResponse<List<ChatDto>>> getChatsOfConversation(
            @PathVariable(name = "id") UUID conversationId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(CommonResponse.success(chatService.getChatsOfConversation(conversationId, principal.userId())));
    }

    @Override
    @PostMapping(value = "/chat/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> askLLM(
            @RequestParam(name = "query") String query,
            @PathVariable(name = "id") UUID conversationId,
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ResponseEntity.ok(chatService.queryLlmStream(query, conversationId, principal.userId()));
    }
}
